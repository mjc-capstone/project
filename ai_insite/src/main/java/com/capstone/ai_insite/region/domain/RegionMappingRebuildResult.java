package com.capstone.ai_insite.region.domain;

import java.time.LocalDate;

public record RegionMappingRebuildResult(
    LocalDate snapshotDate,
    int linkedStoreCount,
    int observedPairCount,
    int autoConfirmedPairCount,
    int candidatePairCount,
    int unresolvedPairCount
) {
}
