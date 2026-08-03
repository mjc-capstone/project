package com.capstone.ai_insite.analysis.entity;

import com.capstone.ai_insite.category.entity.BusinessCategoryEntity;
import com.capstone.ai_insite.analysis.domain.ModelLabelStatus;
import com.capstone.ai_insite.metric.entity.MetricPeriodEntity;
import com.capstone.ai_insite.region.entity.RegionEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
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

    @Column(name = "feature_as_of_date", nullable = false)
    private LocalDate featureAsOfDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "feature_json", nullable = false, columnDefinition = "json")
    private String featureJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "label_json", columnDefinition = "json")
    private String labelJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_period_id")
    private MetricPeriodEntity labelPeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_horizon_period_id")
    private MetricPeriodEntity labelHorizonPeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "label_status", nullable = false, length = 30)
    private ModelLabelStatus labelStatus;

    @Column(name = "label_version", length = 30)
    private String labelVersion;

    @Column(name = "labeled_at")
    private LocalDateTime labeledAt;

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
        this.featureAsOfDate = period.getEndDate();
        this.featureJson = featureJson;
        this.featureVersion = featureVersion;
        this.labelStatus = ModelLabelStatus.PENDING;
    }

    public void applyReadyLabel(
        MetricPeriodEntity targetPeriod,
        MetricPeriodEntity horizonPeriod,
        String labelJson,
        String labelVersion
    ) {
        this.labelPeriod = targetPeriod;
        this.labelHorizonPeriod = horizonPeriod;
        this.labelJson = labelJson;
        this.labelVersion = labelVersion;
        this.labelStatus = ModelLabelStatus.READY;
        this.labeledAt = LocalDateTime.now();
    }

    public void markLabelUnavailable(
        ModelLabelStatus status,
        MetricPeriodEntity targetPeriod,
        String labelVersion
    ) {
        if (status != ModelLabelStatus.MISSING_TARGET
            && status != ModelLabelStatus.INCOMPLETE_SOURCE) {
            throw new IllegalArgumentException("사용할 수 없는 라벨 상태가 아닙니다: " + status);
        }
        this.labelPeriod = targetPeriod;
        this.labelHorizonPeriod = targetPeriod;
        this.labelJson = null;
        this.labelVersion = labelVersion;
        this.labelStatus = status;
        this.labeledAt = LocalDateTime.now();
    }
}
