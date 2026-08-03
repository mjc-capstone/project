package com.capstone.ai_insite.dataimport.dto;

import com.capstone.ai_insite.dataimport.domain.SeoulCommercialHistoryImportResult;
import java.util.List;

public record SeoulCommercialHistoryImportResponse(
    String fromSourcePeriod,
    String toSourcePeriod,
    int quarterCount,
    int totalSalesRowCount,
    int totalStoresRowCount,
    int totalMetricSnapshotCount,
    List<SeoulCommercialImportResponse> quarters
) {
    public static SeoulCommercialHistoryImportResponse from(
        SeoulCommercialHistoryImportResult result
    ) {
        return new SeoulCommercialHistoryImportResponse(
            result.fromSourcePeriod(),
            result.toSourcePeriod(),
            result.quarterCount(),
            result.totalSalesRowCount(),
            result.totalStoresRowCount(),
            result.totalMetricSnapshotCount(),
            result.quarters().stream()
                .map(SeoulCommercialImportResponse::from)
                .toList()
        );
    }
}
