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
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final AssetRepository assetRepository;
    private final VulnerabilityRepository vulnRepository;
    private final RiskScoringService riskScoringService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReportService(AssetRepository assetRepository,
            VulnerabilityRepository vulnRepository,
            RiskScoringService riskScoringService) {
        this.assetRepository = assetRepository;
        this.vulnRepository = vulnRepository;
        this.riskScoringService = riskScoringService;
    }

    // ── JSON Report ───────────────────────────────────────────────────

    public Map<String, Object> generateJsonReport() {
        List<Asset> assets = assetRepository.findAll();
        List<Vulnerability> openVulns = vulnRepository.findByRemediatedFalse();
        List<Vulnerability> sorted = riskScoringService.scoreAndSort(openVulns);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("reportTitle", "VulnGuard Attack Surface Report");
        report.put("generatedAt", LocalDateTime.now().format(FMT));
        report.put("totalAssets", assets.size());
        report.put("openVulnerabilities", openVulns.size());

        Map<String, Long> dist = openVulns.stream()
                .collect(Collectors.groupingBy(v -> v.getSeverity().name(), Collectors.counting()));
        report.put("severityDistribution", dist);

        List<Map<String, Object>> assetReports = assets.stream().map(a -> {
            Map<String, Object> ar = new LinkedHashMap<>();
            ar.put("id", a.getId());
            ar.put("name", a.getName());
            ar.put("type", a.getType().name());
            ar.put("address", a.getAddress());
            ar.put("criticality", a.getCriticality());
            ar.put("lastScanned", a.getLastScannedAt() != null
                    ? a.getLastScannedAt().format(FMT)
                    : "Never");
            ar.put("vulnerabilities", a.getVulnerabilities().stream()
                    .filter(v -> !v.isRemediated())
                    .map(this::vulnToMap)
                    .collect(Collectors.toList()));
            return ar;
        }).collect(Collectors.toList());
        report.put("assets", assetReports);

        report.put("top10Risks", sorted.stream()
                .limit(10)
                .map(this::vulnToMap)
                .collect(Collectors.toList()));

        return report;
    }

    // ── PDF Report ────────────────────────────────────────────────────

    public byte[] generatePdfReport() throws DocumentException {
        List<Vulnerability> openVulns = riskScoringService.scoreAndSort(
                vulnRepository.findByRemediatedFalse());
        List<Asset> assets = assetRepository.findAll();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 54, 36);
        PdfWriter writer = PdfWriter.getInstance(doc, baos);

        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter w, Document d) {
                try {
                    PdfContentByte cb = w.getDirectContent();
                    Phrase footer = new Phrase(
                            "VulnGuard - Confidential  |  Page " + w.getPageNumber(),
                            FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY));
                    ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, footer,
                            (d.left() + d.right()) / 2, d.bottom() - 18, 0);
                } catch (Exception ignored) {
                }
            }
        });

        doc.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, new BaseColor(30, 64, 175));
        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.DARK_GRAY);
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new BaseColor(30, 64, 175));
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);

        // Cover
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph(" "));
        Paragraph title = new Paragraph("VulnGuard", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        Paragraph sub = new Paragraph("Attack Surface Management Report", subFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        doc.add(sub);
        doc.add(new Paragraph(" "));

        Paragraph generated = new Paragraph("Generated: " + LocalDateTime.now().format(FMT), subFont);
        generated.setAlignment(Element.ALIGN_CENTER);
        doc.add(generated);
        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);

        // Executive Summary
        doc.add(new Paragraph("Executive Summary", sectionFont));
        doc.add(new LineSeparator());
        doc.add(Chunk.NEWLINE);

        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(60);
        summaryTable.setHorizontalAlignment(Element.ALIGN_LEFT);
        addTableRow(summaryTable, "Total Assets", String.valueOf(assets.size()), labelFont, bodyFont);
        addTableRow(summaryTable, "Open Vulnerabilities", String.valueOf(openVulns.size()), labelFont, bodyFont);

        Map<String, Long> severityCounts = openVulns.stream()
                .collect(Collectors.groupingBy(v -> v.getSeverity().name(), Collectors.counting()));
        for (Map.Entry<String, Long> entry : severityCounts.entrySet()) {
            addTableRow(summaryTable, entry.getKey() + " Count",
                    String.valueOf(entry.getValue()), labelFont, bodyFont);
        }
        doc.add(summaryTable);
        doc.add(Chunk.NEWLINE);

        // Top 10 Risks
        doc.add(new Paragraph("Top 10 Highest-Risk Vulnerabilities", sectionFont));
        doc.add(new LineSeparator());
        doc.add(Chunk.NEWLINE);

        PdfPTable riskTable = new PdfPTable(new float[] { 2f, 4f, 2f, 2f, 2f, 2f });
        riskTable.setWidthPercentage(100);

        String[] headers = { "CVE ID", "Title", "Severity", "CVSS", "Age(days)", "Risk Score" };
        for (String h : headers) {
            PdfPCell cell = new PdfPCell();
            cell.setBackgroundColor(new BaseColor(30, 64, 175));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPhrase(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE)));
            riskTable.addCell(cell);
        }

        openVulns.stream().limit(10).forEach(v -> {
            riskTable.addCell(new Phrase(v.getCveId(), bodyFont));
            riskTable.addCell(new Phrase(v.getTitle(), bodyFont));
            riskTable.addCell(new Phrase(v.getSeverity().name(), bodyFont));
            riskTable.addCell(new Phrase(String.format("%.1f", v.getCvssScore()), bodyFont));
            riskTable.addCell(new Phrase(String.valueOf(v.getAgeInDays()), bodyFont));
            riskTable.addCell(new Phrase(String.format("%.2f", v.getRiskScore()), bodyFont));
        });
        doc.add(riskTable);
        doc.add(Chunk.NEWLINE);

        // Per-Asset Details
        doc.add(new Paragraph("Asset Vulnerability Details", sectionFont));
        doc.add(new LineSeparator());
        doc.add(Chunk.NEWLINE);

        for (Asset asset : assets) {
            Paragraph assetTitle = new Paragraph(
                    asset.getName() + " [" + asset.getType() + "] - Criticality: "
                            + asset.getCriticality(),
                    labelFont);
            doc.add(assetTitle);

            List<Vulnerability> assetVulns = asset.getVulnerabilities().stream()
                    .filter(v -> !v.isRemediated())
                    .sorted(Comparator.comparingDouble(Vulnerability::getRiskScore).reversed())
                    .collect(Collectors.toList());

            if (assetVulns.isEmpty()) {
                doc.add(new Paragraph("  No open vulnerabilities.", bodyFont));
            } else {
                for (Vulnerability v : assetVulns) {
                    doc.add(new Paragraph(
                            "  * [" + v.getSeverity() + "] " + v.getTitle()
                                    + " (" + v.getCveId() + ") - Risk: " + v.getRiskScore(),
                            bodyFont));
                }
            }
            doc.add(Chunk.NEWLINE);
        }

        doc.close();
        log.info("PDF report generated - {} bytes", baos.size());
        return baos.toByteArray();
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private void addTableRow(PdfPTable table, String label, String value,
            Font labelFont, Font bodyFont) {
        table.addCell(new Phrase(label, labelFont));
        table.addCell(new Phrase(value, bodyFont));
    }

    private Map<String, Object> vulnToMap(Vulnerability v) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", v.getId());
        m.put("cveId", v.getCveId());
        m.put("title", v.getTitle());
        m.put("severity", v.getSeverity().name());
        m.put("cvssScore", Math.round(v.getCvssScore() * 10.0) / 10.0);
        m.put("ageInDays", v.getAgeInDays());
        m.put("riskScore", v.getRiskScore());
        m.put("asset", v.getAsset().getName());
        return m;
    }
}
