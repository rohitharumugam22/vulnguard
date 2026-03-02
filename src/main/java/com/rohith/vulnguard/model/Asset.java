package com.rohith.vulnguard.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rohith.vulnguard.model.enums.AssetType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assets")
public class Asset {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Asset name is required")
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull(message = "Asset type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AssetType type;

    @NotBlank(message = "Asset address is required")
    @Column(nullable = false, length = 255)
    private String address;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    @NotNull @Min(1) @Max(5)
    @Column(nullable = false)
    private Integer criticality;

    @Column(nullable = false)
    private boolean active = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastScannedAt;

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore   // Prevents LazyInitializationException — vulnerabilities are fetched via /api/scans/open, not embedded in Asset
    private List<Vulnerability> vulnerabilities = new ArrayList<>();

    @PrePersist protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public Asset() {}

    public Long getId()                             { return id; }
    public String getName()                         { return name; }
    public AssetType getType()                      { return type; }
    public String getAddress()                      { return address; }
    public String getDescription()                  { return description; }
    public Integer getCriticality()                 { return criticality; }
    public boolean isActive()                       { return active; }
    public LocalDateTime getCreatedAt()             { return createdAt; }
    public LocalDateTime getLastScannedAt()         { return lastScannedAt; }
    public List<Vulnerability> getVulnerabilities() { return vulnerabilities; }

    public void setId(Long v)                                  { id=v; }
    public void setName(String v)                              { name=v; }
    public void setType(AssetType v)                           { type=v; }
    public void setAddress(String v)                           { address=v; }
    public void setDescription(String v)                       { description=v; }
    public void setCriticality(Integer v)                      { criticality=v; }
    public void setActive(boolean v)                           { active=v; }
    public void setCreatedAt(LocalDateTime v)                  { createdAt=v; }
    public void setLastScannedAt(LocalDateTime v)              { lastScannedAt=v; }
    public void setVulnerabilities(List<Vulnerability> v)      { vulnerabilities=v; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private Long id; private String name; private AssetType type;
        private String address, description; private Integer criticality;
        private boolean active = true;
        private LocalDateTime lastScannedAt;
        public Builder id(Long v)               { id=v; return this; }
        public Builder name(String v)           { name=v; return this; }
        public Builder type(AssetType v)        { type=v; return this; }
        public Builder address(String v)        { address=v; return this; }
        public Builder description(String v)    { description=v; return this; }
        public Builder criticality(Integer v)   { criticality=v; return this; }
        public Builder active(boolean v)        { active=v; return this; }
        public Builder lastScannedAt(LocalDateTime v) { lastScannedAt=v; return this; }
        public Asset build() {
            Asset a = new Asset();
            a.id=id; a.name=name; a.type=type; a.address=address;
            a.description=description; a.criticality=criticality;
            a.active=active; a.lastScannedAt=lastScannedAt;
            return a;
        }
    }
}