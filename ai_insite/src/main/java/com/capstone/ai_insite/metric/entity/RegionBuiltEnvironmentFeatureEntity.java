package com.capstone.ai_insite.metric.entity;

import com.capstone.ai_insite.metric.domain.BuiltEnvironmentStatistics;
import com.capstone.ai_insite.region.entity.LegalDongEntity;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "region_built_environment_features")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionBuiltEnvironmentFeatureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private RegionEntity region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "legal_dong_id")
    private LegalDongEntity legalDong;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metric_period_id", nullable = false)
    private MetricPeriodEntity metricPeriod;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "scope_key", nullable = false, length = 100)
    private String scopeKey;

    @Column(name = "region_level", nullable = false, length = 30)
    private String regionLevel;

    @Column(name = "total_building_count", nullable = false)
    private int totalBuildingCount;

    @Column(name = "commercial_building_count", nullable = false)
    private int commercialBuildingCount;

    @Column(name = "avg_building_age", precision = 10, scale = 4)
    private BigDecimal averageBuildingAge;

    @Column(name = "aged_building_ratio", precision = 8, scale = 4)
    private BigDecimal agedBuildingRatio;

    @Column(name = "avg_gross_floor_area", precision = 20, scale = 4)
    private BigDecimal averageGrossFloorArea;

    @Column(name = "total_parking_count", nullable = false)
    private int totalParkingCount;

    @Column(
        name = "parking_spaces_per_commercial_building",
        precision = 16,
        scale = 4
    )
    private BigDecimal parkingSpacesPerCommercialBuilding;

    @Column(name = "commercial_floor_area_proxy", precision = 24, scale = 4)
    private BigDecimal commercialFloorAreaProxy;

    @Column(name = "commercial_floor_area_ratio", precision = 8, scale = 4)
    private BigDecimal commercialFloorAreaRatio;

    @Column(name = "physical_environment_score", precision = 8, scale = 4)
    private BigDecimal physicalEnvironmentScore;

    @Column(name = "source_building_count", nullable = false)
    private int sourceBuildingCount;

    @Column(name = "calculation_version", nullable = false, length = 40)
    private String calculationVersion;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public RegionBuiltEnvironmentFeatureEntity(
        RegionEntity region,
        LegalDongEntity legalDong,
        MetricPeriodEntity metricPeriod,
        LocalDate snapshotDate,
        String scopeKey,
        String regionLevel,
        BuiltEnvironmentStatistics statistics,
        BigDecimal physicalEnvironmentScore,
        String calculationVersion
    ) {
        this.region = region;
        this.legalDong = legalDong;
        this.metricPeriod = metricPeriod;
        this.snapshotDate = snapshotDate;
        this.scopeKey = scopeKey;
        this.regionLevel = regionLevel;
        this.totalBuildingCount = statistics.totalBuildingCount();
        this.commercialBuildingCount = statistics.commercialBuildingCount();
        this.averageBuildingAge = statistics.averageBuildingAge();
        this.agedBuildingRatio = statistics.agedBuildingRatio();
        this.averageGrossFloorArea = statistics.averageGrossFloorArea();
        this.totalParkingCount = statistics.totalParkingCount();
        this.parkingSpacesPerCommercialBuilding =
            statistics.parkingSpacesPerCommercialBuilding();
        this.commercialFloorAreaProxy = statistics.commercialFloorAreaProxy();
        this.commercialFloorAreaRatio = statistics.commercialFloorAreaRatio();
        this.physicalEnvironmentScore = physicalEnvironmentScore;
        this.sourceBuildingCount = statistics.sourceBuildingCount();
        this.calculationVersion = calculationVersion;
    }
}
