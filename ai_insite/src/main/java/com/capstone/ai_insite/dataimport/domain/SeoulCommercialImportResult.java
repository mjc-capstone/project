package com.capstone.ai_insite.dataimport.domain;

public record SeoulCommercialImportResult(
    String sourcePeriodCode,
    String periodCode,
    int salesPageCount,
    int salesRowCount,
    int storesPageCount,
    int storesRowCount,
    int metricSnapshotCount
) {
}
