package com.rohith.vulnguard.model;

import com.rohith.vulnguard.model.enums.AssetType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assets")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Asset name is required")
    @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull(message = "Asset type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AssetType type;

    @NotBlank(message = "Asset address is required")
    @Column(nullable = false, length = 255)
    private String address;

    @Size(max = 500, message = "Description max 500 characters")
    @Column(length = 500)
    private String description;

    @NotNull
    @Min(value = 1, message = "Criticality must be at least 1")
    @Max(value = 5, message = "Criticality cannot exceed 5")
    @Column(nullable = false)
    private Integer criticality;

    @Column(nullable = false)
    private boolean active = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastScannedAt;

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Vulnerability> vulnerabilities = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────────────────
    public Asset() {}

    public Asset(Long id, String name, AssetType type, String address,
                 String description, Integer criticality, boolean active,
                 LocalDateTime createdAt, LocalDateTime lastScannedAt,
                 List<Vulnerability> vulnerabilities) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.address = address;
        this.description = description;
        this.criticality = criticality;
        this.active = active;
        this.createdAt = createdAt;
        this.lastScannedAt = lastScannedAt;
        this.vulnerabilities = vulnerabilities != null ? vulnerabilities : new ArrayList<>();
    }

    // ── Getters ───────────────────────────────────────────────────────
    public Long getId()                            { return id; }
    public String getName()                        { return name; }
    public AssetType getType()                     { return type; }
    public String getAddress()                     { return address; }
    public String getDescription()                 { return description; }
    public Integer getCriticality()                { return criticality; }
    public boolean isActive()                      { return active; }
    public LocalDateTime getCreatedAt()            { return createdAt; }
    public LocalDateTime getLastScannedAt()        { return lastScannedAt; }
    public List<Vulnerability> getVulnerabilities(){ return vulnerabilities; }

    // ── Setters ───────────────────────────────────────────────────────
    public void setId(Long id)                                      { this.id = id; }
    public void setName(String name)                                { this.name = name; }
    public void setType(AssetType type)                             { this.type = type; }
    public void setAddress(String address)                          { this.address = address; }
    public void setDescription(String description)                  { this.description = description; }
    public void setCriticality(Integer criticality)                 { this.criticality = criticality; }
    public void setActive(boolean active)                           { this.active = active; }
    public void setCreatedAt(LocalDateTime createdAt)               { this.createdAt = createdAt; }
    public void setLastScannedAt(LocalDateTime lastScannedAt)       { this.lastScannedAt = lastScannedAt; }
    public void setVulnerabilities(List<Vulnerability> vulns)       { this.vulnerabilities = vulns; }

    // ── Builder ───────────────────────────────────────────────────────
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String name;
        private AssetType type;
        private String address;
        private String description;
        private Integer criticality;
        private boolean active = true;
        private LocalDateTime createdAt;
        private LocalDateTime lastScannedAt;
        private List<Vulnerability> vulnerabilities = new ArrayList<>();

        public Builder id(Long id)                             { this.id = id; return this; }
        public Builder name(String name)                       { this.name = name; return this; }
        public Builder type(AssetType type)                    { this.type = type; return this; }
        public Builder address(String address)                 { this.address = address; return this; }
        public Builder description(String description)         { this.description = description; return this; }
        public Builder criticality(Integer criticality)        { this.criticality = criticality; return this; }
        public Builder active(boolean active)                  { this.active = active; return this; }
        public Builder lastScannedAt(LocalDateTime t)          { this.lastScannedAt = t; return this; }
        public Builder vulnerabilities(List<Vulnerability> v)  { this.vulnerabilities = v; return this; }

        public Asset build() {
            Asset a = new Asset();
            a.id = this.id;
            a.name = this.name;
            a.type = this.type;
            a.address = this.address;
            a.description = this.description;
            a.criticality = this.criticality;
            a.active = this.active;
            a.createdAt = this.createdAt;
            a.lastScannedAt = this.lastScannedAt;
            a.vulnerabilities = this.vulnerabilities;
            return a;
        }
    }
}
