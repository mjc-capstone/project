package com.capstone.ai_insite.category.domain;

public record BusinessCategory(
    Long id,
    String sourceSystem,
    String sourceCategoryCode,
    String sourceCategoryName,
    String largeCategoryName,
    String mediumCategoryName,
    String smallCategoryName,
    String normalizedCategoryCode,
    String normalizedCategoryName
) {
}
