package com.capstone.ai_insite.dataimport.dto.publicdata;

import java.time.LocalDate;

public record SmallBusinessCategoryRow(
    String largeCategoryCode,
    String largeCategoryName,
    String mediumCategoryCode,
    String mediumCategoryName,
    String smallCategoryCode,
    String smallCategoryName,
    LocalDate sourceReferenceDate
) {
}
