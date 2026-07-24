package com.capstone.ai_insite.analysis.entity;

import com.capstone.ai_insite.analysis.domain.AnalysisPrediction;
import com.capstone.ai_insite.analysis.domain.RecommendationGrade;
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
@Table(name = "analysis_results")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_request_id", nullable = false)
    private AnalysisRequestEntity analysisRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_feature_snapshot_id")
    private ModelFeatureSnapshotEntity modelFeatureSnapshot;

    @Column(name = "success_score", precision = 8, scale = 4)
    private BigDecimal successScore;

    @Column(name = "closure_risk_score", precision = 8, scale = 4)
    private BigDecimal closureRiskScore;

    @Column(name = "location_fit_score", precision = 8, scale = 4)
    private BigDecimal locationFitScore;

    @Column(name = "recommendation_grade", length = 10)
    private String recommendationGrade;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "positive_factors_json", columnDefinition = "json")
    private String positiveFactorsJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "risk_factors_json", columnDefinition = "json")
    private String riskFactorsJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "strategy_json", columnDefinition = "json")
    private String strategyJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "model_output_json", columnDefinition = "json")
    private String modelOutputJson;

    @Column(name = "ai_summary", columnDefinition = "text")
    private String aiSummary;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public AnalysisResultEntity(
        AnalysisRequestEntity request,
        ModelFeatureSnapshotEntity featureSnapshot,
        AnalysisPrediction prediction,
        RecommendationGrade grade,
        String positiveFactorsJson,
        String riskFactorsJson,
        String modelOutputJson,
        String summary
    ) {
        this.analysisRequest = request;
        this.modelFeatureSnapshot = featureSnapshot;
        this.successScore = prediction.successScore();
        this.closureRiskScore = prediction.closureRiskScore();
        this.locationFitScore = prediction.locationFitScore();
        this.recommendationGrade = grade.name();
        this.positiveFactorsJson = positiveFactorsJson;
        this.riskFactorsJson = riskFactorsJson;
        this.modelOutputJson = modelOutputJson;
        this.aiSummary = summary;
        this.createdAt = LocalDateTime.now();
    }
}
