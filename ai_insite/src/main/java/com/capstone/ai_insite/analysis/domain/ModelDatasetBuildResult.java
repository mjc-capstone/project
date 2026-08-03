package com.capstone.ai_insite.analysis.domain;

import java.time.LocalDateTime;

public record ModelDatasetBuildResult(
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
    String evaluationMetricsJson,
    LocalDateTime completedAt,
    LocalDateTime createdAt
) {
}
