package com.capstone.ai_insite.analysis.domain;

import java.time.LocalDate;

public record ModelDatasetExample(
    Long featureSnapshotId,
    String regionCode,
    String categoryCode,
    String featurePeriod,
    LocalDate featureAsOfDate,
    String labelPeriod,
    String labelHorizonPeriod,
    DatasetSplit split,
    String featureVersion,
    String labelVersion,
    String featureJson,
    String labelJson
) {
}
