package com.capstone.ai_insite.dataimport.dto;

import com.capstone.ai_insite.dataimport.domain.SeoulCommercialImportResult;

public record SeoulCommercialImportResponse(
    String sourcePeriodCode,
    String periodCode,
    int salesPageCount,
    int salesRowCount,
    int storesPageCount,
    int storesRowCount,
    int metricSnapshotCount
) {
    public static SeoulCommercialImportResponse from(SeoulCommercialImportResult result) {
        return new SeoulCommercialImportResponse(
            result.sourcePeriodCode(),
            result.periodCode(),
            result.salesPageCount(),
            result.salesRowCount(),
            result.storesPageCount(),
            result.storesRowCount(),
            result.metricSnapshotCount()
        );
    }
}
