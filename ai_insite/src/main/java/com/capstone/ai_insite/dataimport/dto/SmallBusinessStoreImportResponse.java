package com.capstone.ai_insite.dataimport.dto;

import com.capstone.ai_insite.dataimport.domain.SmallBusinessStoreImportResult;
import java.time.LocalDate;

public record SmallBusinessStoreImportResponse(
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
    public static SmallBusinessStoreImportResponse from(
        SmallBusinessStoreImportResult result
    ) {
        return new SmallBusinessStoreImportResponse(
            result.jobId(),
            result.standardMonth(),
            result.snapshotDate(),
            result.pageCount(),
            result.fetchedRowCount(),
            result.normalizedRowCount(),
            result.rejectedRowCount(),
            result.regionMappedRowCount(),
            result.categoryMappedRowCount(),
            result.competitionFeatureCount()
        );
    }
}
