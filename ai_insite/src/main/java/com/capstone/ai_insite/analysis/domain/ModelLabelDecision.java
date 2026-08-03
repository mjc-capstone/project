package com.capstone.ai_insite.analysis.domain;

public record ModelLabelDecision(
    ModelLabelStatus status,
    ModelLabelValues values
) {

    public static ModelLabelDecision ready(ModelLabelValues values) {
        return new ModelLabelDecision(ModelLabelStatus.READY, values);
    }

    public static ModelLabelDecision incomplete() {
        return new ModelLabelDecision(ModelLabelStatus.INCOMPLETE_SOURCE, null);
    }
}
