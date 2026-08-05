package com.capstone.ai_insite.dataimport.domain;

import java.util.List;

public record CostHistoryImportResult(
    String fromSourcePeriod,
    String toSourcePeriod,
    int quarterCount,
    int totalRebRows,
    int totalTransactionRows,
    int totalGeneratedFeatures,
    List<CostHistoryQuarterResult> quarters
) {
}
