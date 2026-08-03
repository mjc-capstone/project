package com.capstone.ai_insite.metric.entity;

import com.capstone.ai_insite.region.entity.RegionEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "building_region_mappings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BuildingRegionMappingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_building_id", nullable = false)
    private SourceMolitBuildingRegisterEntity sourceBuilding;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private RegionEntity region;

    @Column(name = "mapping_status", nullable = false, length = 30)
    private String mappingStatus;

    @Column(name = "mapping_confidence", nullable = false, precision = 5, scale = 4)
    private BigDecimal mappingConfidence;

    @Column(name = "mapping_rule", nullable = false, length = 100)
    private String mappingRule;

    @Column(name = "candidate_count", nullable = false)
    private int candidateCount;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public BuildingRegionMappingEntity(
        SourceMolitBuildingRegisterEntity sourceBuilding,
        RegionEntity region,
        String mappingStatus,
        BigDecimal mappingConfidence,
        String mappingRule,
        int candidateCount
    ) {
        this.sourceBuilding = sourceBuilding;
        this.region = region;
        this.mappingStatus = mappingStatus;
        this.mappingConfidence = mappingConfidence;
        this.mappingRule = mappingRule;
        this.candidateCount = candidateCount;
    }

    public boolean isConfirmed() {
        return "CONFIRMED".equals(mappingStatus) && region != null;
    }
}
