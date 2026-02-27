package com.rohith.vulnguard.controller;

import com.rohith.vulnguard.model.enums.Severity;
import com.rohith.vulnguard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "4. Dashboard", description = "Aggregated risk metrics and attack surface overview")
@SecurityRequirement(name = "BearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @Operation(summary = "Get full security dashboard",
               description = "Returns total assets, open vulnerabilities, severity breakdown and top-10 risks.")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }

    @GetMapping("/filter")
    @Operation(summary = "Dashboard filtered by severity",
               description = "Valid values: CRITICAL, HIGH, MEDIUM, LOW, INFO")
    public ResponseEntity<Map<String, Object>> getFiltered(@RequestParam Severity severity) {
        return ResponseEntity.ok(dashboardService.getFilteredDashboard(severity));
    }
}
