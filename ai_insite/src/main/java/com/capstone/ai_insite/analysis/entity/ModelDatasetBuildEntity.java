package com.capstone.ai_insite.analysis.entity;

import com.capstone.ai_insite.analysis.domain.ModelDatasetBuildStatus;
import com.capstone.ai_insite.metric.entity.MetricPeriodEntity;
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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "model_dataset_builds")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ModelDatasetBuildEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dataset_version", nullable = false, length = 50)
    private String datasetVersion;

    @Column(name = "feature_version", nullable = false, length = 30)
    private String featureVersion;

    @Column(name = "label_version", nullable = false, length = 30)
    private String labelVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feature_from_period_id", nullable = false)
    private MetricPeriodEntity featureFromPeriod;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "train_through_period_id", nullable = false)
    private MetricPeriodEntity trainThroughPeriod;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "validation_through_period_id", nullable = false)
    private MetricPeriodEntity validationThroughPeriod;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_through_period_id", nullable = false)
    private MetricPeriodEntity testThroughPeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ModelDatasetBuildStatus status;

    @Column(name = "eligible_feature_count", nullable = false)
    private int eligibleFeatureCount;

    @Column(name = "train_example_count", nullable = false)
    private int trainExampleCount;

    @Column(name = "validation_example_count", nullable = false)
    private int validationExampleCount;

    @Column(name = "test_example_count", nullable = false)
    private int testExampleCount;

    @Column(name = "model_version", length = 50)
    private String modelVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "evaluation_metrics_json", columnDefinition = "json")
    private String evaluationMetricsJson;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public ModelDatasetBuildEntity(
        String datasetVersion,
        String featureVersion,
        String labelVersion,
        MetricPeriodEntity featureFromPeriod,
        MetricPeriodEntity trainThroughPeriod,
        MetricPeriodEntity validationThroughPeriod,
        MetricPeriodEntity testThroughPeriod
    ) {
        this.datasetVersion = datasetVersion;
        this.featureVersion = featureVersion;
        this.labelVersion = labelVersion;
        this.featureFromPeriod = featureFromPeriod;
        this.trainThroughPeriod = trainThroughPeriod;
        this.validationThroughPeriod = validationThroughPeriod;
        this.testThroughPeriod = testThroughPeriod;
        this.status = ModelDatasetBuildStatus.BUILDING;
    }

    public void complete(int train, int validation, int test) {
        if (status != ModelDatasetBuildStatus.BUILDING) {
            throw new IllegalStateException("생성 중인 데이터셋만 완료할 수 있습니다.");
        }
        this.trainExampleCount = train;
        this.validationExampleCount = validation;
        this.testExampleCount = test;
        this.eligibleFeatureCount = train + validation + test;
        this.status = ModelDatasetBuildStatus.READY;
        this.completedAt = LocalDateTime.now();
    }

    public void recordEvaluation(String modelVersion, String metricsJson) {
        if (status != ModelDatasetBuildStatus.READY) {
            throw new IllegalStateException("완료된 데이터셋에만 모델 평가를 기록할 수 있습니다.");
        }
        this.modelVersion = modelVersion;
        this.evaluationMetricsJson = metricsJson;
    }
}
