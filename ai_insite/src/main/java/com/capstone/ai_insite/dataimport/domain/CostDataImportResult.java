package com.capstone.ai_insite.dataimport.domain;

public record CostDataImportResult(
    Long jobId,
    String targetPeriod,
    int requestedPageCount,
    int fetchedRowCount,
    int normalizedRowCount,
    int rejectedRowCount,
    int generatedFeatureCount
) {
}
