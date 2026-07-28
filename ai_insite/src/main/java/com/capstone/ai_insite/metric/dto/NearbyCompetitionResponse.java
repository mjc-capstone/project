package com.capstone.ai_insite.metric.dto;

import java.time.LocalDate;

public record NearbyCompetitionResponse(
    LocalDate snapshotDate,
    int radiusMeters,
    int nearbyStoreCount,
    int sameCategoryStoreCount,
    int mappedCategoryCount,
    int unmappedCategoryStoreCount
) {
}
