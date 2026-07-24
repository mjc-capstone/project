package com.capstone.ai_insite.metric.entity;

import com.capstone.ai_insite.category.entity.BusinessCategoryEntity;
import com.capstone.ai_insite.dataimport.domain.SalesImportCommand;
import com.capstone.ai_insite.dataimport.entity.RawApiPayloadEntity;
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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "source_seoul_sales_by_dong_category_quarter")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SourceSeoulSalesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raw_api_payload_id")
    private RawApiPayloadEntity rawApiPayload;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private RegionEntity region;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_category_id", nullable = false)
    private BusinessCategoryEntity businessCategory;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metric_period_id", nullable = false)
    private MetricPeriodEntity metricPeriod;

    @Column(name = "source_period_code", nullable = false, length = 20)
    private String sourcePeriodCode;

    @Column(name = "region_code_snapshot", nullable = false, length = 20)
    private String regionCodeSnapshot;

    @Column(name = "region_name_snapshot", length = 50)
    private String regionNameSnapshot;

    @Column(name = "category_code_snapshot", nullable = false, length = 30)
    private String categoryCodeSnapshot;

    @Column(name = "category_name_snapshot", length = 100)
    private String categoryNameSnapshot;

    @Column(name = "sales_amount")
    private Long salesAmount;

    @Column(name = "sales_count")
    private Long salesCount;

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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "source_row_json", columnDefinition = "json")
    private String sourceRowJson;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public SourceSeoulSalesEntity(
        RawApiPayloadEntity rawApiPayload,
        RegionEntity region,
        BusinessCategoryEntity category,
        MetricPeriodEntity period
    ) {
        this.rawApiPayload = rawApiPayload;
        this.region = region;
        this.businessCategory = category;
        this.metricPeriod = period;
    }

    public void apply(SalesImportCommand command, RawApiPayloadEntity rawPayload) {
        this.rawApiPayload = rawPayload;
        this.sourcePeriodCode = command.sourcePeriodCode();
        this.regionCodeSnapshot = command.regionCode();
        this.regionNameSnapshot = command.regionName();
        this.categoryCodeSnapshot = command.categoryCode();
        this.categoryNameSnapshot = command.categoryName();
        this.salesAmount = command.salesAmount();
        this.salesCount = command.salesCount();
        this.weekdaySalesAmount = command.weekdaySalesAmount();
        this.weekendSalesAmount = command.weekendSalesAmount();
        this.salesByDayJson = command.salesByDayJson();
        this.salesByTimeJson = command.salesByTimeJson();
        this.salesByDemographicJson = command.salesByDemographicJson();
        this.sourceRowJson = command.sourceRowJson();
    }
}
