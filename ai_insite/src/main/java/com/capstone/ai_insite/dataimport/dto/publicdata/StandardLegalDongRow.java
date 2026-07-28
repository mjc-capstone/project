package com.capstone.ai_insite.dataimport.dto.publicdata;

import java.time.LocalDate;

public record StandardLegalDongRow(
    String legalDongCode,
    String sidoCode,
    String sidoName,
    String sigunguCode,
    String sigunguName,
    String legalDongName,
    LocalDate effectiveFrom
) {
}
