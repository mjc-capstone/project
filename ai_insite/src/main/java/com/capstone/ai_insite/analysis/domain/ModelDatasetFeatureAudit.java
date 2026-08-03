package com.capstone.ai_insite.analysis.domain;

import java.math.BigDecimal;

public record ModelDatasetFeatureAudit(
    long missingCount,
    BigDecimal missingRate
) {
}
