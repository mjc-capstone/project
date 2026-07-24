package com.capstone.ai_insite.analysis.entity;

import com.capstone.ai_insite.category.entity.BusinessCategoryEntity;
import com.capstone.ai_insite.metric.entity.MetricPeriodEntity;
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
@Table(name = "model_feature_snapshots")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ModelFeatureSnapshotEntity {

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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "feature_json", nullable = false, columnDefinition = "json")
    private String featureJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "label_json", columnDefinition = "json")
    private String labelJson;

    @Column(name = "feature_version", nullable = false, length = 30)
    private String featureVersion;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public ModelFeatureSnapshotEntity(
        RegionEntity region,
        BusinessCategoryEntity category,
        MetricPeriodEntity period,
        String featureJson,
        String featureVersion
    ) {
        this.region = region;
        this.businessCategory = category;
        this.metricPeriod = period;
        this.featureJson = featureJson;
        this.featureVersion = featureVersion;
    }
}
