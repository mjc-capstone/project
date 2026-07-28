package com.capstone.ai_insite.dataimport.domain;

public record SmallBusinessStoreBatchResult(
    int normalizedRowCount,
    int regionMappedRowCount,
    int categoryMappedRowCount
) {
}
