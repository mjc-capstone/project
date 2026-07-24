package com.capstone.ai_insite.dataimport.domain;

public record SeoulPageImportResult(
    Long rawPayloadId,
    int importedRowCount,
    int totalSourceRowCount
) {
}
