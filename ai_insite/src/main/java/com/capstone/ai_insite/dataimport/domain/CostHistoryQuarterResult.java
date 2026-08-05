package com.capstone.ai_insite.dataimport.domain;

public record CostHistoryQuarterResult(
    String sourcePeriod,
    CostDataImportResult reb,
    CostDataImportResult transactions
) {
}
