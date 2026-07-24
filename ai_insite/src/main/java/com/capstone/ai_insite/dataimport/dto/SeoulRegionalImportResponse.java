package com.capstone.ai_insite.dataimport.dto;

import com.capstone.ai_insite.dataimport.domain.SeoulRegionalImportResult;

public record SeoulRegionalImportResponse(
    String sourcePeriodCode,
    String periodCode,
    int floatingPopulationPageCount,
    int floatingPopulationRowCount,
    int residentPopulationPageCount,
    int residentPopulationRowCount,
    int workingPopulationPageCount,
    int workingPopulationRowCount,
    int facilitiesPageCount,
    int facilitiesRowCount,
    int apartmentsPageCount,
    int apartmentsRowCount,
    int regionFeatureCount,
    int metricSnapshotCount
) {
    public static SeoulRegionalImportResponse from(SeoulRegionalImportResult result) {
        return new SeoulRegionalImportResponse(
            result.sourcePeriodCode(),
            result.periodCode(),
            result.floatingPopulationPageCount(),
            result.floatingPopulationRowCount(),
            result.residentPopulationPageCount(),
            result.residentPopulationRowCount(),
            result.workingPopulationPageCount(),
            result.workingPopulationRowCount(),
            result.facilitiesPageCount(),
            result.facilitiesRowCount(),
            result.apartmentsPageCount(),
            result.apartmentsRowCount(),
            result.regionFeatureCount(),
            result.metricSnapshotCount()
        );
    }
}
