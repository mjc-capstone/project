package com.capstone.ai_insite.analysis.domain;

import java.time.LocalDate;

public record ModelFeatureInput(
    String requestId,
    String requestedModelReleaseVersion,
    String featureVersion,
    Long featureSnapshotId,
    LocalDate featureAsOfDate,
    MarketFeatureVector marketFeatures,
    UserConditionVector userCondition
) {
}
