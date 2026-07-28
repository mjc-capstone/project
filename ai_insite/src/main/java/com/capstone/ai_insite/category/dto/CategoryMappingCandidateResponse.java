package com.capstone.ai_insite.category.dto;

import com.capstone.ai_insite.category.domain.MappingStatus;
import com.capstone.ai_insite.category.entity.CategoryMappingCandidateEntity;
import java.math.BigDecimal;

public record CategoryMappingCandidateResponse(
    Long id,
    String sourceCode,
    String sourceName,
    Long proposedCategoryId,
    String proposedCategoryCode,
    String proposedCategoryName,
    MappingStatus status,
    BigDecimal confidence,
    String rule,
    long evidenceCount,
    String observedKsicCodesJson,
    String reviewedBy,
    String reviewNote
) {
    public static CategoryMappingCandidateResponse from(
        CategoryMappingCandidateEntity entity
    ) {
        var target = entity.getProposedBusinessCategory();
        return new CategoryMappingCandidateResponse(
            entity.getId(),
            entity.getSmallBusinessCategory().getSmallCategoryCode(),
            entity.getSmallBusinessCategory().getSmallCategoryName(),
            target == null ? null : target.getId(),
            target == null ? null : target.getNormalizedCategoryCode(),
            target == null ? null : target.getNormalizedCategoryName(),
            entity.getMappingStatus(),
            entity.getMappingConfidence(),
            entity.getMappingRule(),
            entity.getEvidenceCount(),
            entity.getObservedKsicCodesJson(),
            entity.getReviewedBy(),
            entity.getReviewNote()
        );
    }
}
