package com.rohith.vulnguard.service;

import com.rohith.vulnguard.model.Vulnerability;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Risk Score = severity.weight x asset.criticality x ageFactor
 * ageFactor  = 1 + (ageInDays / 30.0)
 */
@Service
public class RiskScoringService {

    public double calculateScore(Vulnerability vuln) {
        double severityWeight = vuln.getSeverity().getWeight();
        double criticality    = vuln.getAsset().getCriticality();
        double ageFactor      = 1.0 + (vuln.getAgeInDays() / 30.0);
        return Math.round(severityWeight * criticality * ageFactor * 100.0) / 100.0;
    }

    public List<Vulnerability> scoreAndSort(List<Vulnerability> vulnerabilities) {
        return vulnerabilities.stream()
                .peek(v -> v.setRiskScore(calculateScore(v)))
                .sorted(Comparator.comparingDouble(Vulnerability::getRiskScore).reversed())
                .collect(Collectors.toList());
    }

    public List<Vulnerability> filterByMinScore(List<Vulnerability> vulns, double minScore) {
        return scoreAndSort(vulns).stream()
                .filter(v -> v.getRiskScore() >= minScore)
                .collect(Collectors.toList());
    }
}
