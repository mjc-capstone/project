package com.capstone.ai_insite.metric.entity;

import com.capstone.ai_insite.region.entity.LegalDongEntity;
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
@Table(name = "legal_dong_period_cost_features")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LegalDongPeriodCostFeatureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "legal_dong_id", nullable = false)
    private LegalDongEntity legalDong;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metric_period_id", nullable = false)
    private MetricPeriodEntity metricPeriod;

    @Column(name = "property_type", nullable = false, length = 40)
    private String propertyType;

    @Column(name = "commercial_transaction_count", nullable = false)
    private int commercialTransactionCount;

    @Column(name = "median_commercial_price_per_area", precision = 20, scale = 2)
    private BigDecimal medianCommercialPricePerArea;

    @Column(name = "average_commercial_price_per_area", precision = 20, scale = 2)
    private BigDecimal averageCommercialPricePerArea;

    @Column(name = "price_per_area_p25", precision = 20, scale = 2)
    private BigDecimal pricePerAreaP25;

    @Column(name = "price_per_area_p75", precision = 20, scale = 2)
    private BigDecimal pricePerAreaP75;

    @Column(name = "price_growth_rate", precision = 10, scale = 4)
    private BigDecimal priceGrowthRate;

    @Column(name = "source_transaction_count", nullable = false)
    private int sourceTransactionCount;

    @Column(name = "calculation_version", nullable = false, length = 40)
    private String calculationVersion;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public LegalDongPeriodCostFeatureEntity(
        LegalDongEntity legalDong,
        MetricPeriodEntity metricPeriod,
        String propertyType,
        int commercialTransactionCount,
        BigDecimal medianCommercialPricePerArea,
        BigDecimal averageCommercialPricePerArea,
        BigDecimal pricePerAreaP25,
        BigDecimal pricePerAreaP75,
        BigDecimal priceGrowthRate,
        int sourceTransactionCount,
        String calculationVersion
    ) {
        this.legalDong = legalDong;
        this.metricPeriod = metricPeriod;
        this.propertyType = propertyType;
        this.commercialTransactionCount = commercialTransactionCount;
        this.medianCommercialPricePerArea = medianCommercialPricePerArea;
        this.averageCommercialPricePerArea = averageCommercialPricePerArea;
        this.pricePerAreaP25 = pricePerAreaP25;
        this.pricePerAreaP75 = pricePerAreaP75;
        this.priceGrowthRate = priceGrowthRate;
        this.sourceTransactionCount = sourceTransactionCount;
        this.calculationVersion = calculationVersion;
    }
}
