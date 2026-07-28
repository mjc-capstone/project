package com.capstone.ai_insite.dataimport.dto;

import com.capstone.ai_insite.category.domain.CategoryMappingRebuildResult;
import com.capstone.ai_insite.region.domain.RegionMappingRebuildResult;

public record CodeMappingSynchronizationResponse(
    Long legalDongImportJobId,
    int synchronizedLegalDongCount,
    Long categoryImportJobId,
    int synchronizedSmallCategoryCount,
    RegionMappingRebuildResult regionMappings,
    CategoryMappingRebuildResult categoryMappings
) {
}
