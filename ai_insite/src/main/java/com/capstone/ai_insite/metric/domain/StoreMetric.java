package com.capstone.ai_insite.metric.domain;

import java.math.BigDecimal;

public record StoreMetric(
    Integer storeCount,
    Integer openStoreCount,
    Integer closeStoreCount,
    BigDecimal openRate,
    BigDecimal closeRate,
    BigDecimal franchiseRatio,
    BigDecimal growthRateQoq
) {
}
