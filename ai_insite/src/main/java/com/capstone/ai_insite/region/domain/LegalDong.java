package com.capstone.ai_insite.region.domain;

import java.time.LocalDate;

public record LegalDong(
    Long id,
    String legalDongCode,
    String sidoName,
    String sigunguName,
    String legalDongName,
    LocalDate effectiveFrom,
    LocalDate effectiveTo
) {
}
