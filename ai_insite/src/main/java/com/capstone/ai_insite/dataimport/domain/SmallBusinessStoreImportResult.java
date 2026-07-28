package com.capstone.ai_insite.dataimport.domain;

import java.time.LocalDate;

public record SmallBusinessStoreImportResult(
    Long jobId,
    String standardMonth,
    LocalDate snapshotDate,
    int pageCount,
    long fetchedRowCount,
    long normalizedRowCount,
    long rejectedRowCount,
    long regionMappedRowCount,
    long categoryMappedRowCount,
    int competitionFeatureCount
) {
}
