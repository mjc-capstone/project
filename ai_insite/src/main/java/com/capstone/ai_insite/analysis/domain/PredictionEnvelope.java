package com.capstone.ai_insite.analysis.domain;

public record PredictionEnvelope(
    String requestId,
    String modelReleaseVersion,
    ModelPrediction prediction,
    PredictionQuality quality,
    PredictionSource source,
    boolean fallbackUsed,
    String fallbackReason,
    long inferenceMillis
) {
}
