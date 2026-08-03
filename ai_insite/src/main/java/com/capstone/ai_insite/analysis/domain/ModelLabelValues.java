package com.capstone.ai_insite.analysis.domain;

import java.math.BigDecimal;

public record ModelLabelValues(
    String targetPeriodCode,
    BigDecimal nextQuarterSalesGrowthRate,
    boolean nextQuarterStoreCountDeclined,
    BigDecimal nextQuarterCloseRate,
    String fourQuarterTargetPeriodCode,
    BigDecimal fourQuarterStoreRetentionRate,
    Boolean fourQuarterStoreBaseMaintained
) {
}
