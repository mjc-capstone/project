package com.capstone.ai_insite.metric.domain;

public record CommercialMetric(
    Long snapshotId,
    String regionCode,
    String regionName,
    String categoryCode,
    String categoryName,
    String periodCode,
    SalesMetric sales,
    StoreMetric stores,
    DemandMetric demand,
    MetricScores scores
) {
}
