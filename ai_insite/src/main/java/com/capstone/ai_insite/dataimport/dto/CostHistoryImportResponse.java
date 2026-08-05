package com.capstone.ai_insite.dataimport.dto;

import com.capstone.ai_insite.dataimport.domain.CostHistoryImportResult;
import java.util.List;

public record CostHistoryImportResponse(
    String fromSourcePeriod,
    String toSourcePeriod,
    int quarterCount,
    int totalRebRows,
    int totalTransactionRows,
    int totalGeneratedFeatures,
    List<Quarter> quarters
) {
    public static CostHistoryImportResponse from(CostHistoryImportResult result) {
        return new CostHistoryImportResponse(
            result.fromSourcePeriod(),
            result.toSourcePeriod(),
            result.quarterCount(),
            result.totalRebRows(),
            result.totalTransactionRows(),
            result.totalGeneratedFeatures(),
            result.quarters().stream()
                .map(value -> new Quarter(
                    value.sourcePeriod(),
                    CostDataImportResponse.from(value.reb()),
                    CostDataImportResponse.from(value.transactions())
                ))
                .toList()
        );
    }

    public record Quarter(
        String sourcePeriod,
        CostDataImportResponse reb,
        CostDataImportResponse transactions
    ) {
    }
}
