package com.rohith.vulnguard.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Hidden
public class RootController {

    @GetMapping("/")
    public RedirectView root() {
        return new RedirectView("/index.html");
    }

    @GetMapping("/api/info")
    public Map<String, Object> info() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("application", "VulnGuard — Enhanced Attack Surface Management Simulator");
        info.put("version",     "1.0.0");
        info.put("status",      "Running");
        info.put("timestamp",   LocalDateTime.now().toString());
        info.put("ui",          "http://localhost:8080/index.html");
        info.put("swagger",     "http://localhost:8080/swagger-ui.html");
        info.put("apiDocs",     "http://localhost:8080/v3/api-docs");
        return info;
    }
}
