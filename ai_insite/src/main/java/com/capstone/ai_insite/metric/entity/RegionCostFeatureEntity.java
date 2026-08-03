package com.capstone.ai_insite.metric.entity;

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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "region_cost_features")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionCostFeatureEntity {

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

    @Column(name = "source_system", nullable = false, length = 40)
    private String sourceSystem;

    @Column(name = "scope_key", nullable = false, length = 150)
    private String scopeKey;

    @Column(name = "region_level", nullable = false, length = 30)
    private String regionLevel;

    @Column(name = "source_region_code", length = 30)
    private String sourceRegionCode;

    @Column(name = "source_region_name", length = 100)
    private String sourceRegionName;

    @Column(name = "property_type", length = 50)
    private String propertyType;

    @Column(name = "rent_amount", precision = 18, scale = 2)
    private BigDecimal rentAmount;

    @Column(name = "rent_index", precision = 12, scale = 4)
    private BigDecimal rentIndex;

    @Column(name = "vacancy_rate", precision = 8, scale = 4)
    private BigDecimal vacancyRate;

    @Column(name = "investment_return_rate", precision = 8, scale = 4)
    private BigDecimal investmentReturnRate;

    @Column(name = "commercial_transaction_count")
    private Integer commercialTransactionCount;

    @Column(name = "median_commercial_price_per_area", precision = 18, scale = 2)
    private BigDecimal medianCommercialPricePerArea;

    @Column(name = "price_growth_rate", precision = 8, scale = 4)
    private BigDecimal priceGrowthRate;

    @Column(name = "rent_pressure_score", precision = 8, scale = 4)
    private BigDecimal rentPressureScore;

    @Column(name = "vacancy_risk_score", precision = 8, scale = 4)
    private BigDecimal vacancyRiskScore;

    @Column(name = "fixed_cost_burden_index", precision = 8, scale = 4)
    private BigDecimal fixedCostBurdenIndex;

    @Column(name = "location_cost_score", precision = 8, scale = 4)
    private BigDecimal locationCostScore;

    @Column(name = "source_observation_count", nullable = false)
    private int sourceObservationCount;

    @Column(name = "rent_unit", length = 50)
    private String rentUnit;

    @Column(name = "price_unit", length = 50)
    private String priceUnit;

    @Column(name = "calculation_version", nullable = false, length = 40)
    private String calculationVersion;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public RegionCostFeatureEntity(
        RegionEntity region,
        LegalDongEntity legalDong,
        MetricPeriodEntity metricPeriod,
        String sourceSystem,
        String scopeKey,
        String regionLevel,
        String sourceRegionCode,
        String sourceRegionName,
        String propertyType,
        BigDecimal rentAmount,
        BigDecimal rentIndex,
        BigDecimal vacancyRate,
        BigDecimal investmentReturnRate,
        Integer commercialTransactionCount,
        BigDecimal medianCommercialPricePerArea,
        BigDecimal priceGrowthRate,
        BigDecimal rentPressureScore,
        BigDecimal vacancyRiskScore,
        BigDecimal fixedCostBurdenIndex,
        BigDecimal locationCostScore,
        int sourceObservationCount,
        String rentUnit,
        String priceUnit,
        String calculationVersion
    ) {
        this.region = region;
        this.legalDong = legalDong;
        this.metricPeriod = metricPeriod;
        this.sourceSystem = sourceSystem;
        this.scopeKey = scopeKey;
        this.regionLevel = regionLevel;
        this.sourceRegionCode = sourceRegionCode;
        this.sourceRegionName = sourceRegionName;
        this.propertyType = propertyType;
        this.rentAmount = rentAmount;
        this.rentIndex = rentIndex;
        this.vacancyRate = vacancyRate;
        this.investmentReturnRate = investmentReturnRate;
        this.commercialTransactionCount = commercialTransactionCount;
        this.medianCommercialPricePerArea = medianCommercialPricePerArea;
        this.priceGrowthRate = priceGrowthRate;
        this.rentPressureScore = rentPressureScore;
        this.vacancyRiskScore = vacancyRiskScore;
        this.fixedCostBurdenIndex = fixedCostBurdenIndex;
        this.locationCostScore = locationCostScore;
        this.sourceObservationCount = sourceObservationCount;
        this.rentUnit = rentUnit;
        this.priceUnit = priceUnit;
        this.calculationVersion = calculationVersion;
    }
}
