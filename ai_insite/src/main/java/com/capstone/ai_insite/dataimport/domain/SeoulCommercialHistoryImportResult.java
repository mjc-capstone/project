package com.capstone.ai_insite.dataimport.domain;

import java.util.List;

public record SeoulCommercialHistoryImportResult(
    String fromSourcePeriod,
    String toSourcePeriod,
    int quarterCount,
    int totalSalesRowCount,
    int totalStoresRowCount,
    int totalMetricSnapshotCount,
    List<SeoulCommercialImportResult> quarters
) {
}
