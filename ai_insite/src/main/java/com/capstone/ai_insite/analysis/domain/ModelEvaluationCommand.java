package com.capstone.ai_insite.analysis.domain;

public record ModelEvaluationCommand(
    String modelVersion,
    String evaluationMetricsJson
) {

    public ModelEvaluationCommand {
        if (modelVersion == null || modelVersion.isBlank()) {
            throw new IllegalArgumentException("modelVersion은 필수입니다.");
        }
        if (evaluationMetricsJson == null || evaluationMetricsJson.isBlank()) {
            throw new IllegalArgumentException("evaluationMetrics는 필수입니다.");
        }
        modelVersion = modelVersion.trim();
    }
}
