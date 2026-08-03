package com.capstone.ai_insite.analysis.domain;

import java.util.Map;

public record ModelDatasetLabelAudit(
    long availableCount,
    long missingCount,
    Long trueCount,
    Long falseCount,
    Map<DatasetSplit, Long> availableBySplit
) {
}
