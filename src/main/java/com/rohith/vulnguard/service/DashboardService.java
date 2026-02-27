package com.rohith.vulnguard.service;

import com.rohith.vulnguard.model.Vulnerability;
import com.rohith.vulnguard.model.enums.Severity;
import com.rohith.vulnguard.repository.AssetRepository;
import com.rohith.vulnguard.repository.VulnerabilityRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
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

        long totalAssets  = assetRepository.count();
        long activeAssets = assetRepository.findByActiveTrue().size();
        dashboard.put("totalAssets",  totalAssets);
        dashboard.put("activeAssets", activeAssets);

        long openVulns = vulnRepository.countOpen();
        dashboard.put("openVulnerabilities", openVulns);

        List<Vulnerability> allOpen = vulnRepository.findByRemediatedFalse();
        Map<String, Long> bySeverity = allOpen.stream()
                .collect(Collectors.groupingBy(v -> v.getSeverity().name(), Collectors.counting()));
        dashboard.put("severityBreakdown", bySeverity);

        List<Map<String, Object>> topRisks = riskScoringService.scoreAndSort(allOpen).stream()
                .limit(10)
                .map(this::vulnSummary)
                .collect(Collectors.toList());
        dashboard.put("top10Risks", topRisks);

        OptionalDouble avgScore = allOpen.stream()
                .mapToDouble(Vulnerability::getRiskScore)
                .average();
        dashboard.put("averageRiskScore",
                avgScore.isPresent() ? Math.round(avgScore.getAsDouble() * 100.0) / 100.0 : 0.0);

        return dashboard;
    }

    public Map<String, Object> getFilteredDashboard(Severity severity) {
        List<Vulnerability> filtered = vulnRepository.findByRemediatedFalse().stream()
                .filter(v -> v.getSeverity() == severity)
                .sorted(Comparator.comparingDouble(Vulnerability::getRiskScore).reversed())
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("filter", severity.name());
        result.put("count", filtered.size());
        result.put("vulnerabilities", filtered.stream()
                .map(this::vulnSummary)
                .collect(Collectors.toList()));
        return result;
    }

    private Map<String, Object> vulnSummary(Vulnerability v) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",               v.getId());
        map.put("cveId",            v.getCveId());
        map.put("title",            v.getTitle());
        map.put("severity",         v.getSeverity().name());
        map.put("cvssScore",        v.getCvssScore());
        map.put("ageInDays",        v.getAgeInDays());
        map.put("riskScore",        v.getRiskScore());
        map.put("asset",            v.getAsset().getName());
        map.put("assetCriticality", v.getAsset().getCriticality());
        return map;
    }
}
