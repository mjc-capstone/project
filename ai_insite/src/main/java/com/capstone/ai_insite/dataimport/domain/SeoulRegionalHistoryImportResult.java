package com.capstone.ai_insite.dataimport.domain;

import java.util.List;

public record SeoulRegionalHistoryImportResult(
    String fromSourcePeriod,
    String toSourcePeriod,
    int quarterCount,
    long totalFloatingPopulationRows,
    long totalResidentPopulationRows,
    long totalWorkingPopulationRows,
    long totalFacilitiesRows,
    long totalApartmentsRows,
    int totalRegionFeatureCount,
    List<SeoulRegionalImportResult> quarters
) {
}
