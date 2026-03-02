package com.rohith.vulnguard.controller;

import com.itextpdf.text.DocumentException;
import com.rohith.vulnguard.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "5. Reports", description = "Export attack surface reports as JSON or PDF")
@SecurityRequirement(name = "BearerAuth")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/json")
    @Operation(summary = "Export full report as JSON")
    public ResponseEntity<Map<String, Object>> jsonReport() {
        return ResponseEntity.ok(reportService.generateJsonReport());
    }

    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Export full report as PDF",
               description = "Downloads a professionally formatted PDF report.")
    public ResponseEntity<byte[]> pdfReport() throws DocumentException {
        byte[] pdfBytes = reportService.generatePdfReport();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("vulnguard-report.pdf").build());
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}
