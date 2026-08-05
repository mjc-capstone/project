package com.capstone.ai_insite.dataimport.dto;

import com.capstone.ai_insite.dataimport.domain.SeoulRegionalHistoryImportResult;
import java.util.List;

public record SeoulRegionalHistoryImportResponse(
    String fromSourcePeriod,
    String toSourcePeriod,
    int quarterCount,
    long totalFloatingPopulationRows,
    long totalResidentPopulationRows,
    long totalWorkingPopulationRows,
    long totalFacilitiesRows,
    long totalApartmentsRows,
    int totalRegionFeatureCount,
    List<SeoulRegionalImportResponse> quarters
) {

    public static SeoulRegionalHistoryImportResponse from(
        SeoulRegionalHistoryImportResult result
    ) {
        return new SeoulRegionalHistoryImportResponse(
            result.fromSourcePeriod(),
            result.toSourcePeriod(),
            result.quarterCount(),
            result.totalFloatingPopulationRows(),
            result.totalResidentPopulationRows(),
            result.totalWorkingPopulationRows(),
            result.totalFacilitiesRows(),
            result.totalApartmentsRows(),
            result.totalRegionFeatureCount(),
            result.quarters().stream()
                .map(SeoulRegionalImportResponse::from)
                .toList()
        );
    }
}
