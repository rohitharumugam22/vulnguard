package com.rohith.vulnguard.service;

import com.rohith.vulnguard.model.Asset;
import com.rohith.vulnguard.model.enums.AssetType;
import com.rohith.vulnguard.repository.AssetRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    public Asset getById(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Asset not found: " + id));
    }

    public List<Asset> getByType(AssetType type) {
        return assetRepository.findByType(type);
    }

    public List<Asset> searchByKeyword(String keyword) {
        String kw = keyword.toLowerCase();
        return assetRepository.findAll().stream()
                .filter(a -> a.isActive() &&
                        (a.getName().toLowerCase().contains(kw) ||
                         a.getAddress().toLowerCase().contains(kw) ||
                         (a.getDescription() != null && a.getDescription().toLowerCase().contains(kw))))
                .collect(Collectors.toList());
    }

    public List<Asset> getByCriticality(int minCriticality) {
        return assetRepository.findByCriticalityGreaterThanEqual(minCriticality);
    }

    public Asset create(Asset asset) {
        return assetRepository.save(asset);
    }

    public Asset update(Long id, Asset updated) {
        Asset existing = getById(id);
        existing.setName(updated.getName());
        existing.setType(updated.getType());
        existing.setAddress(updated.getAddress());
        existing.setDescription(updated.getDescription());
        existing.setCriticality(updated.getCriticality());
        existing.setActive(updated.isActive());
        return assetRepository.save(existing);
    }

    public void delete(Long id) {
        Asset asset = getById(id);
        asset.setActive(false);
        assetRepository.save(asset);
    }

    public void hardDelete(Long id) {
        assetRepository.deleteById(id);
    }

    public long countActive() {
        return assetRepository.findByActiveTrue().size();
    }
}
