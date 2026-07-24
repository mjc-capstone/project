package com.capstone.ai_insite.region.dto;

import com.capstone.ai_insite.region.domain.LegalDong;
import java.time.LocalDate;

public record LegalDongResponse(
    String legalDongCode,
    String sidoName,
    String sigunguName,
    String legalDongName,
    LocalDate effectiveFrom,
    LocalDate effectiveTo
) {
    public static LegalDongResponse from(LegalDong legalDong) {
        return new LegalDongResponse(
            legalDong.legalDongCode(),
            legalDong.sidoName(),
            legalDong.sigunguName(),
            legalDong.legalDongName(),
            legalDong.effectiveFrom(),
            legalDong.effectiveTo()
        );
    }
}
