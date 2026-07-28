package com.capstone.ai_insite.category.domain;

import java.time.LocalDate;

public record CategoryMappingRebuildResult(
    LocalDate snapshotDate,
    int sourceCategoryCount,
    int autoConfirmedCount,
    int candidateCount,
    int unresolvedCount,
    int remappedStoreCount
) {
}
