package com.capstone.ai_insite.metric.entity;

import com.capstone.ai_insite.dataimport.dto.reb.RebCommercialRentObservation;
import com.capstone.ai_insite.dataimport.entity.RawApiPayloadEntity;
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
@Table(name = "source_reb_commercial_rent_stats")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SourceRebCommercialRentStatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "raw_api_payload_id", nullable = false)
    private RawApiPayloadEntity rawApiPayload;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metric_period_id", nullable = false)
    private MetricPeriodEntity metricPeriod;

    @Column(name = "statistic_table_id", nullable = false, length = 40)
    private String statisticTableId;

    @Column(name = "source_region_code", nullable = false, length = 30)
    private String sourceRegionCode;

    @Column(name = "source_region_name", nullable = false, length = 100)
    private String sourceRegionName;

    @Column(name = "source_region_full_name", nullable = false, length = 300)
    private String sourceRegionFullName;

    @Column(name = "region_level", nullable = false, length = 30)
    private String regionLevel;

    @Column(name = "property_type", nullable = false, length = 40)
    private String propertyType;

    @Column(name = "metric_type", nullable = false, length = 40)
    private String metricType;

    @Column(name = "metric_value", nullable = false, precision = 20, scale = 8)
    private BigDecimal metricValue;

    @Column(name = "unit_name", length = 50)
    private String unitName;

    @Column(name = "source_item_code", length = 30)
    private String sourceItemCode;

    @Column(name = "source_item_name", length = 100)
    private String sourceItemName;

    @Column(name = "source_row_json", nullable = false, columnDefinition = "json")
    private String sourceRowJson;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public SourceRebCommercialRentStatEntity(
        RawApiPayloadEntity rawApiPayload,
        MetricPeriodEntity metricPeriod,
        RebCommercialRentObservation observation
    ) {
        this.rawApiPayload = rawApiPayload;
        this.metricPeriod = metricPeriod;
        this.statisticTableId = observation.statisticTableId();
        this.sourceRegionCode = observation.sourceRegionCode();
        this.sourceRegionName = observation.sourceRegionName();
        this.sourceRegionFullName = observation.sourceRegionFullName();
        this.regionLevel = observation.regionLevel();
        this.propertyType = observation.propertyType().name();
        this.metricType = observation.metricType().name();
        this.metricValue = observation.value();
        this.unitName = observation.unitName();
        this.sourceItemCode = observation.sourceItemCode();
        this.sourceItemName = observation.sourceItemName();
        this.sourceRowJson = observation.sourceRowJson();
    }
}
