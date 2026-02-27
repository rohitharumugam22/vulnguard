package com.rohith.vulnguard.controller;

import com.rohith.vulnguard.model.Asset;
import com.rohith.vulnguard.model.enums.AssetType;
import com.rohith.vulnguard.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import org.springframework.lang.NonNull;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@Tag(name = "2. Asset Management", description = "Create and manage attack surface assets")
@SecurityRequirement(name = "BearerAuth")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    @Operation(summary = "List all active assets")
    public ResponseEntity<List<Asset>> getAllAssets() {
        return ResponseEntity.ok(assetService.getAllActive());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get asset by ID")
    public ResponseEntity<Asset> getAsset(@PathVariable @NonNull Long id) {
        return ResponseEntity.ok(assetService.getById(id));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Filter assets by type", description = "Valid types: DOMAIN, IP_ADDRESS, API_ENDPOINT, CLOUD_RESOURCE, WEB_APPLICATION")
    public ResponseEntity<List<Asset>> getByType(@PathVariable AssetType type) {
        return ResponseEntity.ok(assetService.getByType(type));
    }

    @PostMapping
    @Operation(summary = "Create a new asset", description = "Criticality 1 (low) to 5 (critical). Higher criticality amplifies risk scores.")
    public ResponseEntity<Asset> createAsset(@Valid @RequestBody @NonNull Asset asset) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assetService.create(asset));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing asset")
    public ResponseEntity<Asset> updateAsset(@PathVariable @NonNull Long id,
            @Valid @RequestBody Asset asset) {
        return ResponseEntity.ok(assetService.update(id, asset));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete an asset (marks inactive)")
    public ResponseEntity<Void> deleteAsset(@PathVariable @NonNull Long id) {
        assetService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
