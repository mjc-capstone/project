package com.capstone.ai_insite.analysis.dto;

import com.capstone.ai_insite.analysis.domain.ModelDatasetBuildResult;
import com.capstone.ai_insite.analysis.domain.ModelDatasetBuildStatus;
import java.time.LocalDateTime;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public record ModelDatasetBuildResponse(
    Long id,
    String datasetVersion,
    String featureVersion,
    String labelVersion,
    String featureFromPeriod,
    String trainThroughPeriod,
    String validationThroughPeriod,
    String testThroughPeriod,
    ModelDatasetBuildStatus status,
    int eligibleFeatureCount,
    int trainExampleCount,
    int validationExampleCount,
    int testExampleCount,
    String modelVersion,
    JsonNode evaluationMetrics,
    LocalDateTime completedAt,
    LocalDateTime createdAt
) {

    public static ModelDatasetBuildResponse from(
        ModelDatasetBuildResult result,
        ObjectMapper objectMapper
    ) {
        try {
            JsonNode metrics = result.evaluationMetricsJson() == null
                ? null
                : objectMapper.readTree(result.evaluationMetricsJson());
            return new ModelDatasetBuildResponse(
                result.id(),
                result.datasetVersion(),
                result.featureVersion(),
                result.labelVersion(),
                result.featureFromPeriod(),
                result.trainThroughPeriod(),
                result.validationThroughPeriod(),
                result.testThroughPeriod(),
                result.status(),
                result.eligibleFeatureCount(),
                result.trainExampleCount(),
                result.validationExampleCount(),
                result.testExampleCount(),
                result.modelVersion(),
                metrics,
                result.completedAt(),
                result.createdAt()
            );
        } catch (Exception exception) {
            throw new IllegalStateException("모델 평가지표 JSON 해석에 실패했습니다.", exception);
        }
    }
}
