package com.rohith.vulnguard.controller;

import com.rohith.vulnguard.model.Vulnerability;
import com.rohith.vulnguard.model.enums.Severity;
import com.rohith.vulnguard.service.VulnerabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scans")
@Tag(name = "3. Vulnerability Scanning", description = "Trigger scans and manage discovered vulnerabilities")
@SecurityRequirement(name = "BearerAuth")
public class ScanController {

    private final VulnerabilityService vulnerabilityService;

    public ScanController(VulnerabilityService vulnerabilityService) {
        this.vulnerabilityService = vulnerabilityService;
    }

    @PostMapping("/asset/{assetId}")
    @Operation(summary = "Trigger a simulated scan for one asset",
               description = "Generates 3–5 realistic CVE-style vulnerabilities for the given asset.")
    public ResponseEntity<List<Vulnerability>> scan(@PathVariable @NonNull Long assetId) {
        return ResponseEntity.ok(vulnerabilityService.simulateScan(assetId));
    }

    @PostMapping("/all")
    @Operation(summary = "Batch scan — scan ALL active assets at once")
    public ResponseEntity<Map<String, Object>> scanAll() {
        return ResponseEntity.ok(vulnerabilityService.scanAllAssets());
    }

    @GetMapping("/asset/{assetId}")
    @Operation(summary = "Get all vulnerabilities for one asset")
    public ResponseEntity<List<Vulnerability>> getByAsset(@PathVariable @NonNull Long assetId) {
        return ResponseEntity.ok(vulnerabilityService.getByAsset(assetId));
    }

    @GetMapping("/open")
    @Operation(summary = "Get all open vulnerabilities sorted by risk score")
    public ResponseEntity<List<Vulnerability>> getOpen() {
        return ResponseEntity.ok(vulnerabilityService.getAllOpen());
    }

    @GetMapping("/severity/{severity}")
    @Operation(summary = "Filter vulnerabilities by severity",
               description = "Valid values: CRITICAL, HIGH, MEDIUM, LOW, INFO")
    public ResponseEntity<List<Vulnerability>> getBySeverity(@PathVariable Severity severity) {
        return ResponseEntity.ok(vulnerabilityService.getBySeverity(severity));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get overall vulnerability statistics")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(vulnerabilityService.getStats());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vulnerability by ID")
    public ResponseEntity<Vulnerability> getById(@PathVariable @NonNull Long id) {
        return ResponseEntity.ok(vulnerabilityService.getById(id));
    }

    @PatchMapping("/{id}/remediate")
    @Operation(summary = "Mark a vulnerability as remediated")
    public ResponseEntity<Map<String, Object>> remediate(@PathVariable @NonNull Long id) {
        Vulnerability v = vulnerabilityService.markRemediated(id);
        return ResponseEntity.ok(Map.of(
                "message",    "Vulnerability marked as remediated",
                "id",         v.getId(),
                "cveId",      v.getCveId(),
                "remediated", v.isRemediated()
        ));
    }
}
