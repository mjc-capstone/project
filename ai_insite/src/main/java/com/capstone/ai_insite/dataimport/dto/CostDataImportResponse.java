package com.capstone.ai_insite.dataimport.dto;

import com.capstone.ai_insite.dataimport.domain.CostDataImportResult;

public record CostDataImportResponse(
    Long jobId,
    String targetPeriod,
    int requestedPageCount,
    int fetchedRowCount,
    int normalizedRowCount,
    int rejectedRowCount,
    int generatedFeatureCount
) {

    public static CostDataImportResponse from(CostDataImportResult result) {
        return new CostDataImportResponse(
            result.jobId(),
            result.targetPeriod(),
            result.requestedPageCount(),
            result.fetchedRowCount(),
            result.normalizedRowCount(),
            result.rejectedRowCount(),
            result.generatedFeatureCount()
        );
    }
}
