package com.capstone.ai_insite.category.dto;

public record CategoryMappingReviewRequest(
    Long businessCategoryId,
    String reviewedBy,
    String note
) {
}
