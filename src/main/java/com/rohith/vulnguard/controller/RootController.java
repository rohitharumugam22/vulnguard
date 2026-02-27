package com.rohith.vulnguard.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Hidden
public class RootController {

    @GetMapping("/")
    public Map<String, Object> root() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("application", "VulnGuard - Intelligent Attack Surface Management Simulator");
        info.put("version",     "1.0.0");
        info.put("status",      "Running");
        info.put("timestamp",   LocalDateTime.now().toString());
        info.put("swagger",     "http://localhost:8080/swagger-ui.html");
        info.put("apiDocs",     "http://localhost:8080/v3/api-docs");
        info.put("quickStart",  Map.of(
                "step1", "POST /api/auth/register  - create account",
                "step2", "POST /api/auth/login     - get JWT token",
                "step3", "POST /api/assets         - add an asset",
                "step4", "POST /api/scans/asset/{id} - trigger scan",
                "step5", "GET  /api/dashboard      - view risks",
                "step6", "GET  /api/reports/pdf    - download report"
        ));
        return info;
    }
}
