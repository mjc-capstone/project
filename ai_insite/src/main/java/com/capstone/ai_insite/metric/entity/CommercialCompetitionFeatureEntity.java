package com.capstone.ai_insite.metric.entity;

import com.capstone.ai_insite.category.entity.BusinessCategoryEntity;
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
@Table(name = "commercial_competition_features")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommercialCompetitionFeatureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private RegionEntity region;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_category_id", nullable = false)
    private BusinessCategoryEntity businessCategory;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metric_period_id", nullable = false)
    private MetricPeriodEntity metricPeriod;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "active_store_count", nullable = false)
    private int activeStoreCount;

    @Column(name = "store_count_per_square_km", precision = 12, scale = 4)
    private BigDecimal storeCountPerSquareKm;

    @Column(name = "same_category_store_count", nullable = false)
    private int sameCategoryStoreCount;

    @Column(name = "franchise_store_count")
    private Integer franchiseStoreCount;

    @Column(name = "category_diversity_index", precision = 12, scale = 6)
    private BigDecimal categoryDiversityIndex;

    @Column(name = "source_store_snapshot_count", nullable = false)
    private int sourceStoreSnapshotCount;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public CommercialCompetitionFeatureEntity(
        RegionEntity region,
        BusinessCategoryEntity businessCategory,
        MetricPeriodEntity metricPeriod,
        LocalDate snapshotDate
    ) {
        this.region = region;
        this.businessCategory = businessCategory;
        this.metricPeriod = metricPeriod;
        this.snapshotDate = snapshotDate;
    }

    public void update(
        int activeStoreCount,
        int sameCategoryStoreCount,
        BigDecimal categoryDiversityIndex,
        int sourceStoreSnapshotCount
    ) {
        this.activeStoreCount = activeStoreCount;
        this.sameCategoryStoreCount = sameCategoryStoreCount;
        this.categoryDiversityIndex = categoryDiversityIndex;
        this.sourceStoreSnapshotCount = sourceStoreSnapshotCount;
        this.storeCountPerSquareKm = null;
        this.franchiseStoreCount = null;
    }
}
