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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "commercial_metric_snapshots")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommercialMetricSnapshotEntity {

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

    @Column(name = "sales_amount")
    private Long salesAmount;

    @Column(name = "sales_count")
    private Long salesCount;

    @Column(name = "avg_ticket_amount", precision = 18, scale = 2)
    private BigDecimal avgTicketAmount;

    @Column(name = "weekday_sales_amount")
    private Long weekdaySalesAmount;

    @Column(name = "weekend_sales_amount")
    private Long weekendSalesAmount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sales_by_day_json", columnDefinition = "json")
    private String salesByDayJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sales_by_time_json", columnDefinition = "json")
    private String salesByTimeJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sales_by_demographic_json", columnDefinition = "json")
    private String salesByDemographicJson;

    @Column(name = "store_count")
    private Integer storeCount;

    @Column(name = "normal_store_count")
    private Integer normalStoreCount;

    @Column(name = "franchise_store_count")
    private Integer franchiseStoreCount;

    @Column(name = "open_store_count")
    private Integer openStoreCount;

    @Column(name = "close_store_count")
    private Integer closeStoreCount;

    @Column(name = "open_rate", precision = 8, scale = 4)
    private BigDecimal openRate;

    @Column(name = "close_rate", precision = 8, scale = 4)
    private BigDecimal closeRate;

    @Column(name = "sales_growth_rate_qoq", precision = 24, scale = 4)
    private BigDecimal salesGrowthRateQoq;

    @Column(name = "sales_growth_rate_yoy", precision = 24, scale = 4)
    private BigDecimal salesGrowthRateYoy;

    @Column(name = "store_growth_rate_qoq", precision = 24, scale = 4)
    private BigDecimal storeGrowthRateQoq;

    @Column(name = "franchise_ratio", precision = 8, scale = 4)
    private BigDecimal franchiseRatio;

    @Column(name = "competition_intensity_score", precision = 8, scale = 4)
    private BigDecimal competitionIntensityScore;

    @Column(name = "demand_score", precision = 8, scale = 4)
    private BigDecimal demandScore;

    @Column(name = "market_score", precision = 8, scale = 4)
    private BigDecimal marketScore;

    @Column(name = "stability_score", precision = 8, scale = 4)
    private BigDecimal stabilityScore;

    @Column(name = "closure_risk_signal", precision = 8, scale = 4)
    private BigDecimal closureRiskSignal;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public CommercialMetricSnapshotEntity(
        RegionEntity region,
        BusinessCategoryEntity category,
        MetricPeriodEntity period
    ) {
        this.region = region;
        this.businessCategory = category;
        this.metricPeriod = period;
    }

    public void applySources(
        SourceSeoulSalesEntity sales,
        SourceSeoulStoresEntity stores,
        BigDecimal salesGrowthRateQoq,
        BigDecimal storeGrowthRateQoq
    ) {
        this.salesAmount = sales.getSalesAmount();
        this.salesCount = sales.getSalesCount();
        this.avgTicketAmount = averageTicket(sales.getSalesAmount(), sales.getSalesCount());
        this.weekdaySalesAmount = sales.getWeekdaySalesAmount();
        this.weekendSalesAmount = sales.getWeekendSalesAmount();
        this.salesByDayJson = sales.getSalesByDayJson();
        this.salesByTimeJson = sales.getSalesByTimeJson();
        this.salesByDemographicJson = sales.getSalesByDemographicJson();
        this.storeCount = stores.getStoreCount();
        this.normalStoreCount = stores.getNormalStoreCount();
        this.franchiseStoreCount = stores.getFranchiseStoreCount();
        this.openStoreCount = stores.getOpenStoreCount();
        this.closeStoreCount = stores.getCloseStoreCount();
        this.openRate = stores.getOpenRate();
        this.closeRate = stores.getCloseRate();
        this.salesGrowthRateQoq = salesGrowthRateQoq;
        this.storeGrowthRateQoq = storeGrowthRateQoq;
        this.franchiseRatio = ratio(stores.getFranchiseStoreCount(), stores.getStoreCount());
    }

    public void applyScores(
        BigDecimal competitionScore,
        BigDecimal demandScore,
        BigDecimal marketScore,
        BigDecimal stabilityScore,
        BigDecimal closureRiskSignal
    ) {
        this.competitionIntensityScore = competitionScore;
        this.demandScore = demandScore;
        this.marketScore = marketScore;
        this.stabilityScore = stabilityScore;
        this.closureRiskSignal = closureRiskSignal;
    }

    private static BigDecimal averageTicket(Long salesAmount, Long salesCount) {
        if (salesAmount == null || salesCount == null || salesCount == 0) {
            return null;
        }
        return BigDecimal.valueOf(salesAmount)
            .divide(BigDecimal.valueOf(salesCount), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal ratio(Integer part, Integer total) {
        if (part == null || total == null || total == 0) {
            return null;
        }
        return BigDecimal.valueOf(part)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);
    }
}
