package com.capstone.ai_insite.category.dto;

import com.capstone.ai_insite.category.domain.BusinessCategory;

public record BusinessCategoryResponse(
    String sourceSystem,
    String categoryCode,
    String categoryName,
    String largeCategoryName,
    String mediumCategoryName,
    String smallCategoryName,
    String normalizedCategoryCode,
    String normalizedCategoryName
) {
    public static BusinessCategoryResponse from(BusinessCategory category) {
        return new BusinessCategoryResponse(
            category.sourceSystem(),
            category.sourceCategoryCode(),
            category.sourceCategoryName(),
            category.largeCategoryName(),
            category.mediumCategoryName(),
            category.smallCategoryName(),
            category.normalizedCategoryCode(),
            category.normalizedCategoryName()
        );
    }
}
