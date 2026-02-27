package com.rohith.vulnguard.service;

import com.rohith.vulnguard.model.Asset;
import com.rohith.vulnguard.model.enums.AssetType;
import com.rohith.vulnguard.repository.AssetRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;

import java.util.List;

@Service
@Transactional
public class AssetService {

    private final AssetRepository assetRepository;

    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    public List<Asset> getAllActive() {
        return assetRepository.findByActiveTrue();
    }

    public List<Asset> getAll() {
        return assetRepository.findAll();
    }

    public Asset getById(@NonNull Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Asset not found: " + id));
    }

    public List<Asset> getByType(AssetType type) {
        return assetRepository.findByType(type);
    }

    // ✅ Removed @SuppressWarnings("null") — not needed
    public Asset create(@NonNull Asset asset) {
        return assetRepository.save(asset);
    }

    // ✅ Removed @SuppressWarnings("null") — not needed
    public Asset update(@NonNull Long id, Asset updated) {
        Asset existing = getById(id);
        existing.setName(updated.getName());
        existing.setType(updated.getType());
        existing.setAddress(updated.getAddress());
        existing.setDescription(updated.getDescription());
        existing.setCriticality(updated.getCriticality());
        existing.setActive(updated.isActive());
        return assetRepository.save(existing);
    }

    public void delete(@NonNull Long id) {
        Asset asset = getById(id);
        asset.setActive(false);
        assetRepository.save(asset);
    }

    public void hardDelete(@NonNull Long id) {
        assetRepository.deleteById(id);
    }
}