package com.capstone.ai_insite.analysis.dto;

import com.capstone.ai_insite.analysis.domain.PredictionEnvelope;
import java.util.List;

public record ModelPredictionResponse(
    String requestId,
    String modelReleaseVersion,
    double nextQuarterSalesGrowthRate,
    double storeDeclineRiskIndex,
    double nextQuarterCloseRate,
    double fourQuarterStoreRetentionRate,
    double storeBaseMaintainedIndex,
    boolean inDistribution,
    double missingFeatureRate,
    List<String> warnings,
    String predictionSource,
    long inferenceMillis
) {

    public static ModelPredictionResponse from(PredictionEnvelope envelope) {
        var prediction = envelope.prediction();
        return new ModelPredictionResponse(
            envelope.requestId(),
            envelope.modelReleaseVersion(),
            prediction.nextQuarterSalesGrowthRate(),
            prediction.storeDeclineProbability(),
            prediction.nextQuarterCloseRate(),
            prediction.fourQuarterStoreRetentionRate(),
            prediction.storeBaseMaintainedProbability(),
            envelope.quality().inDistribution(),
            envelope.quality().missingFeatureRate(),
            envelope.quality().warnings(),
            envelope.source().name(),
            envelope.inferenceMillis()
        );
    }
}
