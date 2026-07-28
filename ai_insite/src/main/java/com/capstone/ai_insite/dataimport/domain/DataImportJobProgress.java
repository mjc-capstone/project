package com.capstone.ai_insite.dataimport.domain;

public record DataImportJobProgress(
    int pageCount,
    long fetchedRowCount,
    long normalizedRowCount,
    long rejectedRowCount
) {
    public DataImportJobProgress {
        if (pageCount < 0
            || fetchedRowCount < 0
            || normalizedRowCount < 0
            || rejectedRowCount < 0) {
            throw new IllegalArgumentException("수집 작업 카운터는 음수일 수 없습니다.");
        }
        if (fetchedRowCount != normalizedRowCount + rejectedRowCount) {
            throw new IllegalArgumentException(
                "수집 행 수는 정규화 행과 제외 행의 합이어야 합니다."
            );
        }
    }
}
