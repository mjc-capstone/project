package com.capstone.ai_insite.metric.entity;

import com.capstone.ai_insite.metric.domain.RegionPeriodFeatureValues;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "region_period_features")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionPeriodFeatureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private RegionEntity region;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metric_period_id", nullable = false)
    private MetricPeriodEntity metricPeriod;

    @Column(name = "floating_population_total")
    private Long floatingPopulationTotal;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "floating_population_by_age_json", columnDefinition = "json")
    private String floatingPopulationByAgeJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "floating_population_by_time_json", columnDefinition = "json")
    private String floatingPopulationByTimeJson;

    @Column(name = "resident_population_total")
    private Long residentPopulationTotal;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resident_population_by_age_json", columnDefinition = "json")
    private String residentPopulationByAgeJson;

    @Column(name = "household_count")
    private Long householdCount;

    @Column(name = "working_population_total")
    private Long workingPopulationTotal;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "working_population_by_age_json", columnDefinition = "json")
    private String workingPopulationByAgeJson;

    @Column(name = "facility_total_count")
    private Integer facilityTotalCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "facility_detail_json", columnDefinition = "json")
    private String facilityDetailJson;

    @Column(name = "apartment_complex_count")
    private Integer apartmentComplexCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "apartment_detail_json", columnDefinition = "json")
    private String apartmentDetailJson;

    @Column(name = "daytime_population_ratio", precision = 8, scale = 4)
    private BigDecimal daytimePopulationRatio;

    @Column(name = "night_population_ratio", precision = 8, scale = 4)
    private BigDecimal nightPopulationRatio;

    @Column(name = "weekend_population_ratio", precision = 8, scale = 4)
    private BigDecimal weekendPopulationRatio;

    @Column(name = "residential_demand_score", precision = 8, scale = 4)
    private BigDecimal residentialDemandScore;

    @Column(name = "office_demand_score", precision = 8, scale = 4)
    private BigDecimal officeDemandScore;

    @Column(name = "attraction_score", precision = 8, scale = 4)
    private BigDecimal attractionScore;

    @Column(name = "traffic_access_score", precision = 8, scale = 4)
    private BigDecimal trafficAccessScore;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public RegionPeriodFeatureEntity(
        RegionEntity region,
        MetricPeriodEntity metricPeriod
    ) {
        this.region = region;
        this.metricPeriod = metricPeriod;
    }

    public void apply(RegionPeriodFeatureValues values) {
        floatingPopulationTotal = values.floatingPopulationTotal();
        floatingPopulationByAgeJson = values.floatingPopulationByAgeJson();
        floatingPopulationByTimeJson = values.floatingPopulationByTimeJson();
        residentPopulationTotal = values.residentPopulationTotal();
        residentPopulationByAgeJson = values.residentPopulationByAgeJson();
        householdCount = values.householdCount();
        workingPopulationTotal = values.workingPopulationTotal();
        workingPopulationByAgeJson = values.workingPopulationByAgeJson();
        facilityTotalCount = values.facilityTotalCount();
        facilityDetailJson = values.facilityDetailJson();
        apartmentComplexCount = values.apartmentComplexCount();
        apartmentDetailJson = values.apartmentDetailJson();
        daytimePopulationRatio = values.daytimePopulationRatio();
        nightPopulationRatio = values.nightPopulationRatio();
        weekendPopulationRatio = values.weekendPopulationRatio();
        residentialDemandScore = values.residentialDemandScore();
        officeDemandScore = values.officeDemandScore();
        attractionScore = values.attractionScore();
        trafficAccessScore = values.trafficAccessScore();
    }
}
