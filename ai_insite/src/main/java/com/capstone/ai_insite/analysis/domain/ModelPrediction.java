package com.capstone.ai_insite.analysis.domain;

public record ModelPrediction(
    double nextQuarterSalesGrowthRate,
    double storeDeclineProbability,
    double nextQuarterCloseRate,
    double fourQuarterStoreRetentionRate,
    double storeBaseMaintainedProbability
) {
}
