package com.capstone.ai_insite.analysis.domain;

public record MarketFeatureVector(
    String categoryCode,
    String regionCode,
    Double salesAmount,
    Double salesCount,
    Double salesGrowthRateQoq,
    Double storeCount,
    Double storeGrowthRateQoq,
    Double demandScore,
    Double competitionScore,
    Double marketScore,
    Double stabilityScore,
    Double closureRiskSignal,
    Double floatingPopulation,
    Double residentPopulation,
    Double workingPopulation
) {
}
