package com.rohith.vulnguard.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.rohith.vulnguard.model.Asset;
import com.rohith.vulnguard.model.Vulnerability;
import com.rohith.vulnguard.repository.AssetRepository;
import com.rohith.vulnguard.repository.VulnerabilityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

@Service
@Transactional(readOnly = true)   // ✅ FIX: was missing — caused LazyInitializationException on asset.getVulnerabilities()
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AssetRepository assetRepository;
    private final VulnerabilityRepository vulnRepository;
    private final RiskScoringService riskScoringService;

    public ReportService(AssetRepository assetRepository,
                         VulnerabilityRepository vulnRepository,
                         RiskScoringService riskScoringService) {
        this.assetRepository = assetRepository;
        this.vulnRepository = vulnRepository;
        this.riskScoringService = riskScoringService;
    }

    // ── JSON Report ───────────────────────────────────────────────────

    public Map<String, Object> generateJsonReport() {
        List<Asset> assets       = assetRepository.findAll();
        List<Vulnerability> open = vulnRepository.findByRemediatedFalse();
        open.forEach(v -> v.setRiskScore(riskScoringService.calculateScore(v)));
        open.sort(Comparator.comparingDouble(Vulnerability::getRiskScore).reversed());

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("reportTitle",        "VulnGuard Attack Surface Report");
        report.put("generatedAt",        LocalDateTime.now().format(FMT));
        report.put("totalAssets",        assets.size());
        report.put("openVulnerabilities",open.size());

        Map<String, Long> dist = open.stream()
                .collect(Collectors.groupingBy(v -> v.getSeverity().name(), Collectors.counting()));
        report.put("severityDistribution", dist);

        // ✅ asset.getVulnerabilities() safe inside @Transactional
        List<Map<String, Object>> assetReports = assets.stream().map(a -> {
            Map<String, Object> ar = new LinkedHashMap<>();
            ar.put("id",          a.getId());
            ar.put("name",        a.getName());
            ar.put("type",        a.getType().name());
            ar.put("address",     a.getAddress());
            ar.put("criticality", a.getCriticality());
            ar.put("lastScanned", a.getLastScannedAt() != null ? a.getLastScannedAt().format(FMT) : "Never");
            ar.put("vulnerabilities", a.getVulnerabilities().stream()
                    .filter(v -> !v.isRemediated()).map(this::vulnToMap).collect(Collectors.toList()));
            return ar;
        }).collect(Collectors.toList());
        report.put("assets", assetReports);
        report.put("top10Risks", open.stream().limit(10).map(this::vulnToMap).collect(Collectors.toList()));
        return report;
    }

    // ── PDF Report ────────────────────────────────────────────────────

    public byte[] generatePdfReport() throws DocumentException {
        List<Vulnerability> open = vulnRepository.findByRemediatedFalse();
        open.forEach(v -> v.setRiskScore(riskScoringService.calculateScore(v)));
        open.sort(Comparator.comparingDouble(Vulnerability::getRiskScore).reversed());
        List<Asset> assets = assetRepository.findAll();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 54, 36);
        PdfWriter writer = PdfWriter.getInstance(doc, baos);

        writer.setPageEvent(new PdfPageEventHelper() {
            @Override public void onEndPage(PdfWriter w, Document d) {
                try {
                    ColumnText.showTextAligned(w.getDirectContent(), Element.ALIGN_CENTER,
                        new Phrase("VulnGuard - Confidential  |  Page " + w.getPageNumber(),
                            FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY)),
                        (d.left() + d.right()) / 2, d.bottom() - 18, 0);
                } catch (Exception ignored) {}
            }
        });

        doc.open();

        Font titleFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, new BaseColor(30, 64, 175));
        Font subFont     = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.DARK_GRAY);
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new BaseColor(30, 64, 175));
        Font bodyFont    = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
        Font labelFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);

        doc.add(new Paragraph(" ")); doc.add(new Paragraph(" "));
        Paragraph title = new Paragraph("VulnGuard", titleFont); title.setAlignment(Element.ALIGN_CENTER); doc.add(title);
        Paragraph sub   = new Paragraph("Attack Surface Management Report", subFont); sub.setAlignment(Element.ALIGN_CENTER); doc.add(sub);
        doc.add(new Paragraph(" "));
        Paragraph gen   = new Paragraph("Generated: " + LocalDateTime.now().format(FMT), subFont); gen.setAlignment(Element.ALIGN_CENTER); doc.add(gen);
        doc.add(Chunk.NEWLINE); doc.add(Chunk.NEWLINE);

        doc.add(new Paragraph("Executive Summary", sectionFont)); doc.add(new LineSeparator()); doc.add(Chunk.NEWLINE);

        PdfPTable summary = new PdfPTable(2); summary.setWidthPercentage(60); summary.setHorizontalAlignment(Element.ALIGN_LEFT);
        addRow(summary, "Total Assets",          String.valueOf(assets.size()), labelFont, bodyFont);
        addRow(summary, "Open Vulnerabilities",  String.valueOf(open.size()),   labelFont, bodyFont);
        Map<String, Long> sevCount = open.stream().collect(Collectors.groupingBy(v -> v.getSeverity().name(), Collectors.counting()));
        for (Map.Entry<String, Long> e : sevCount.entrySet())
            addRow(summary, e.getKey() + " Count", String.valueOf(e.getValue()), labelFont, bodyFont);
        doc.add(summary); doc.add(Chunk.NEWLINE);

        doc.add(new Paragraph("Top 10 Highest-Risk Vulnerabilities", sectionFont)); doc.add(new LineSeparator()); doc.add(Chunk.NEWLINE);

        PdfPTable riskTable = new PdfPTable(new float[]{2f, 4f, 2f, 2f, 2f, 2f}); riskTable.setWidthPercentage(100);
        for (String h : new String[]{"CVE ID", "Title", "Severity", "CVSS", "Age(d)", "Risk"}) {
            PdfPCell c = new PdfPCell(); c.setBackgroundColor(new BaseColor(30, 64, 175));
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setPhrase(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE)));
            riskTable.addCell(c);
        }
        open.stream().limit(10).forEach(v -> {
            riskTable.addCell(new Phrase(v.getCveId(), bodyFont));
            riskTable.addCell(new Phrase(v.getTitle(), bodyFont));
            riskTable.addCell(new Phrase(v.getSeverity().name(), bodyFont));
            riskTable.addCell(new Phrase(String.format("%.1f", v.getCvssScore()), bodyFont));
            riskTable.addCell(new Phrase(String.valueOf(v.getAgeInDays()), bodyFont));
            riskTable.addCell(new Phrase(String.format("%.2f", v.getRiskScore()), bodyFont));
        });
        doc.add(riskTable); doc.add(Chunk.NEWLINE);

        doc.add(new Paragraph("Asset Vulnerability Details", sectionFont)); doc.add(new LineSeparator()); doc.add(Chunk.NEWLINE);

        // ✅ asset.getVulnerabilities() safe inside @Transactional
        for (Asset asset : assets) {
            doc.add(new Paragraph(asset.getName() + " [" + asset.getType() + "] - Criticality: " + asset.getCriticality(), labelFont));
            List<Vulnerability> avulns = asset.getVulnerabilities().stream()
                    .filter(v -> !v.isRemediated())
                    .sorted(Comparator.comparingDouble(Vulnerability::getRiskScore).reversed())
                    .collect(Collectors.toList());
            if (avulns.isEmpty()) {
                doc.add(new Paragraph("  No open vulnerabilities.", bodyFont));
            } else {
                for (Vulnerability v : avulns)
                    doc.add(new Paragraph("  * [" + v.getSeverity() + "] " + v.getTitle() + " (" + v.getCveId() + ") - Risk: " + v.getRiskScore(), bodyFont));
            }
            doc.add(Chunk.NEWLINE);
        }

        doc.close();
        log.info("PDF report generated - {} bytes", baos.size());
        return baos.toByteArray();
    }

    private void addRow(PdfPTable t, String l, String v, Font lf, Font bf) {
        t.addCell(new Phrase(l, lf)); t.addCell(new Phrase(v, bf));
    }

    private Map<String, Object> vulnToMap(Vulnerability v) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",        v.getId());
        m.put("cveId",     v.getCveId());
        m.put("title",     v.getTitle());
        m.put("severity",  v.getSeverity().name());
        m.put("cvssScore", Math.round(v.getCvssScore() * 10.0) / 10.0);
        m.put("ageInDays", v.getAgeInDays());
        m.put("riskScore", v.getRiskScore());
        m.put("asset",     v.getAsset().getName());
        return m;
    }
}
