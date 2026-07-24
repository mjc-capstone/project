package com.capstone.ai_insite.dataimport.domain;

public record SeoulRegionalImportResult(
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
}
