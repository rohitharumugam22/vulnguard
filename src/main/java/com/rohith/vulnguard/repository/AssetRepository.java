package com.rohith.vulnguard.repository;

import com.rohith.vulnguard.model.Asset;
import com.rohith.vulnguard.model.enums.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByActiveTrue();
    List<Asset> findByType(AssetType type);
    List<Asset> findByCriticalityGreaterThanEqual(int minCriticality);
}
