package com.capstone.ai_insite.dataimport.dto;

public record ImportResultResponse(
    Long normalizedRowId,
    String status
) {
    public static ImportResultResponse completed(Long normalizedRowId) {
        return new ImportResultResponse(normalizedRowId, "COMPLETED");
    }
}
