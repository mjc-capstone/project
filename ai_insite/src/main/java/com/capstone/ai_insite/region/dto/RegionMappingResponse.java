package com.capstone.ai_insite.region.dto;

import com.capstone.ai_insite.region.domain.RegionMappingStatus;
import com.capstone.ai_insite.region.entity.AdministrativeLegalDongMappingEntity;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RegionMappingResponse(
    Long id,
    String administrativeDongCode,
    String administrativeDongName,
    String legalDongCode,
    String legalDongName,
    RegionMappingStatus status,
    BigDecimal confidence,
    String rule,
    long evidenceCount,
    LocalDate effectiveFrom,
    LocalDate effectiveTo,
    String reviewedBy,
    String reviewNote
) {
    public static RegionMappingResponse from(
        AdministrativeLegalDongMappingEntity entity
    ) {
        return new RegionMappingResponse(
            entity.getId(),
            entity.getRegion().getAdministrativeDongCode(),
            entity.getRegion().getAdministrativeDongName(),
            entity.getLegalDong().getLegalDongCode(),
            entity.getLegalDong().getLegalDongName(),
            entity.getMappingStatus(),
            entity.getMappingConfidence(),
            entity.getMappingRule(),
            entity.getEvidenceCount(),
            entity.getEffectiveFrom(),
            entity.getEffectiveTo(),
            entity.getReviewedBy(),
            entity.getReviewNote()
        );
    }
}
