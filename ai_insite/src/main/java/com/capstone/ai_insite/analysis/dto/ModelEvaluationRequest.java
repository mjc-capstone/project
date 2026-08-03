package com.capstone.ai_insite.analysis.dto;

import com.capstone.ai_insite.analysis.domain.ModelEvaluationCommand;
import tools.jackson.databind.JsonNode;

public record ModelEvaluationRequest(
    String modelVersion,
    JsonNode evaluationMetrics
) {

    public ModelEvaluationCommand toCommand() {
        if (evaluationMetrics == null || evaluationMetrics.isNull()) {
            throw new IllegalArgumentException("evaluationMetrics는 필수입니다.");
        }
        return new ModelEvaluationCommand(modelVersion, evaluationMetrics.toString());
    }
}
