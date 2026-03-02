package com.rohith.vulnguard.service;

import com.rohith.vulnguard.model.Asset;
import com.rohith.vulnguard.model.Vulnerability;
import com.rohith.vulnguard.model.enums.Severity;
import com.rohith.vulnguard.repository.AssetRepository;
import com.rohith.vulnguard.repository.VulnerabilityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)   // ✅ FIX: was missing — caused LazyInitializationException on asset.getVulnerabilities()
public class DashboardService {

    private final AssetRepository assetRepository;
    private final VulnerabilityRepository vulnRepository;
    private final RiskScoringService riskScoringService;

    public DashboardService(AssetRepository assetRepository,
                            VulnerabilityRepository vulnRepository,
                            RiskScoringService riskScoringService) {
        this.assetRepository = assetRepository;
        this.vulnRepository = vulnRepository;
        this.riskScoringService = riskScoringService;
    }

    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new LinkedHashMap<>();

        List<Asset> allAssets   = assetRepository.findAll();
        List<Asset> activeAssets = assetRepository.findByActiveTrue();
        dashboard.put("totalAssets",  allAssets.size());
        dashboard.put("activeAssets", activeAssets.size());

        List<Vulnerability> allVulns  = vulnRepository.findAll();
        List<Vulnerability> openVulns = vulnRepository.findByRemediatedFalse();
        long remediatedCount = allVulns.size() - openVulns.size();

        dashboard.put("openVulnerabilities",       openVulns.size());
        dashboard.put("totalVulnerabilities",      allVulns.size());
        dashboard.put("remediatedVulnerabilities", remediatedCount);
        dashboard.put("remediationRate",
            allVulns.isEmpty() ? 0.0
                : Math.round((remediatedCount * 100.0 / allVulns.size()) * 10.0) / 10.0);

        Map<String, Long> bySeverity = openVulns.stream()
                .collect(Collectors.groupingBy(v -> v.getSeverity().name(), Collectors.counting()));
        dashboard.put("severityBreakdown", bySeverity);
        dashboard.put("criticalCount", bySeverity.getOrDefault("CRITICAL", 0L));
        dashboard.put("highCount",     bySeverity.getOrDefault("HIGH",     0L));
        dashboard.put("mediumCount",   bySeverity.getOrDefault("MEDIUM",   0L));
        dashboard.put("lowCount",      bySeverity.getOrDefault("LOW",      0L));

        // Score vulns in memory (no DB write needed here)
        List<Vulnerability> scored = new ArrayList<>(openVulns);
        scored.forEach(v -> v.setRiskScore(riskScoringService.calculateScore(v)));
        scored.sort(Comparator.comparingDouble(Vulnerability::getRiskScore).reversed());

        dashboard.put("top10Risks", scored.stream().limit(10).map(this::vulnSummary).collect(Collectors.toList()));

        OptionalDouble avg = scored.stream().mapToDouble(Vulnerability::getRiskScore).average();
        OptionalDouble max = scored.stream().mapToDouble(Vulnerability::getRiskScore).max();
        dashboard.put("averageRiskScore", avg.isPresent() ? Math.round(avg.getAsDouble() * 100.0) / 100.0 : 0.0);
        dashboard.put("maxRiskScore",     max.isPresent() ? Math.round(max.getAsDouble() * 100.0) / 100.0 : 0.0);

        long scannedCount = activeAssets.stream().filter(a -> a.getLastScannedAt() != null).count();
        dashboard.put("assetsScanned",      scannedCount);
        dashboard.put("assetsNeverScanned", activeAssets.size() - scannedCount);

        return dashboard;
    }

    public Map<String, Object> getFilteredDashboard(Severity severity) {
        List<Vulnerability> filtered = vulnRepository.findByRemediatedFalse().stream()
                .filter(v -> v.getSeverity() == severity)
                .peek(v -> v.setRiskScore(riskScoringService.calculateScore(v)))
                .sorted(Comparator.comparingDouble(Vulnerability::getRiskScore).reversed())
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("filter", severity.name());
        result.put("count",  filtered.size());
        result.put("vulnerabilities", filtered.stream().map(this::vulnSummary).collect(Collectors.toList()));
        return result;
    }

    public Map<String, Object> getTrend() {
        List<Vulnerability> all = vulnRepository.findAll();

        Map<String, Long> discoveredByDate = all.stream()
                .filter(v -> v.getDiscoveredAt() != null)
                .collect(Collectors.groupingBy(
                        v -> v.getDiscoveredAt().toLocalDate().toString(), Collectors.counting()));

        Map<String, Long> remediatedByDate = all.stream()
                .filter(v -> v.isRemediated() && v.getDiscoveredAt() != null)
                .collect(Collectors.groupingBy(
                        v -> v.getDiscoveredAt().toLocalDate().toString(), Collectors.counting()));

        Map<String, Long> trend    = new TreeMap<>();
        Map<String, Long> remTrend = new TreeMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 13; i >= 0; i--) {
            String date = today.minusDays(i).toString();
            trend.put(date,    discoveredByDate.getOrDefault(date, 0L));
            remTrend.put(date, remediatedByDate.getOrDefault(date, 0L));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("discovered", trend);
        result.put("remediated", remTrend);
        result.put("labels",     new ArrayList<>(trend.keySet()));
        return result;
    }

    public List<Map<String, Object>> getAssetRiskSummary() {
        // ✅ FIX: @Transactional on class ensures Hibernate session is open when we access
        //         asset.getVulnerabilities() (LAZY collection) — previously caused LazyInitializationException
        return assetRepository.findByActiveTrue().stream()
                .map(asset -> {
                    List<Vulnerability> openVulns = asset.getVulnerabilities().stream()
                            .filter(v -> !v.isRemediated())
                            .peek(v -> v.setRiskScore(riskScoringService.calculateScore(v)))
                            .collect(Collectors.toList());

                    double maxRisk = openVulns.stream().mapToDouble(Vulnerability::getRiskScore).max().orElse(0.0);
                    String topSeverity = openVulns.stream()
                            .max(Comparator.comparingDouble(v -> v.getSeverity().getWeight()))
                            .map(v -> v.getSeverity().name()).orElse("NONE");
                    Map<String, Long> sevBreak = openVulns.stream()
                            .collect(Collectors.groupingBy(v -> v.getSeverity().name(), Collectors.counting()));

                    Map<String, Object> s = new LinkedHashMap<>();
                    s.put("assetId",          asset.getId());
                    s.put("assetName",         asset.getName());
                    s.put("type",              asset.getType().name());
                    s.put("address",           asset.getAddress());
                    s.put("criticality",       asset.getCriticality());
                    s.put("openVulns",         openVulns.size());
                    s.put("maxRiskScore",      Math.round(maxRisk * 100.0) / 100.0);
                    s.put("topSeverity",       topSeverity);
                    s.put("severityBreakdown", sevBreak);
                    s.put("lastScanned",       asset.getLastScannedAt() != null ? asset.getLastScannedAt().toString() : null);
                    return s;
                })
                .sorted(Comparator.comparingDouble(m -> -((Number) m.get("maxRiskScore")).doubleValue()))
                .collect(Collectors.toList());
    }

    private Map<String, Object> vulnSummary(Vulnerability v) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",               v.getId());
        m.put("cveId",            v.getCveId());
        m.put("title",            v.getTitle());
        m.put("description",      v.getDescription());
        m.put("severity",         v.getSeverity().name());
        m.put("cvssScore",        Math.round(v.getCvssScore() * 10.0) / 10.0);
        m.put("ageInDays",        v.getAgeInDays());
        m.put("riskScore",        v.getRiskScore());
        m.put("asset",            v.getAsset().getName());
        m.put("assetId",          v.getAsset().getId());
        m.put("assetCriticality", v.getAsset().getCriticality());
        return m;
    }
}
