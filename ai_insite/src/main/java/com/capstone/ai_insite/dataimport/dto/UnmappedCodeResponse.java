package com.capstone.ai_insite.dataimport.dto;

import com.capstone.ai_insite.dataimport.domain.UnmappedCodeType;
import java.time.LocalDate;

public record UnmappedCodeResponse(
    UnmappedCodeType type,
    String sourceCode,
    String sourceName,
    long rowCount,
    LocalDate snapshotDate
) {
}
