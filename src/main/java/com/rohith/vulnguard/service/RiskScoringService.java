package com.rohith.vulnguard.service;

import com.rohith.vulnguard.model.Vulnerability;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Calculates composite risk scores for vulnerabilities.
 *
 * Formula: severity.weight × criticality × (1 + ageInDays / 30)
 *
 * - severity.weight:  CRITICAL=10, HIGH=7, MEDIUM=4, LOW=1, INFO=0.5
 * - criticality:      Asset criticality 1-5
 * - age factor:       Older vulnerabilities accumulate higher risk
 */
@Service
public class RiskScoringService {

    public double calculateScore(Vulnerability vuln) {
        double severityWeight = vuln.getSeverity().getWeight();
        double criticality    = vuln.getAsset() != null ? vuln.getAsset().getCriticality() : 1.0;
        double ageFactor      = 1.0 + (vuln.getAgeInDays() / 30.0);
        double raw            = severityWeight * criticality * ageFactor;
        // Normalise to 0-100 scale  (max theoretical: 10 * 5 * (1 + 180/30) = 350)
        return Math.min(100.0, Math.round((raw / 350.0) * 100.0 * 100.0) / 100.0);
    }

    public List<Vulnerability> scoreAndSort(List<Vulnerability> vulns) {
        vulns.forEach(v -> v.setRiskScore(calculateScore(v)));
        return vulns.stream()
                .sorted(Comparator.comparingDouble(Vulnerability::getRiskScore).reversed())
                .collect(Collectors.toList());
    }
}
