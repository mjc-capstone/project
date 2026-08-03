package com.capstone.ai_insite.analysis.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ModelDatasetAudit(
    Long datasetId,
    String datasetVersion,
    String featureVersion,
    String labelVersion,
    ModelDatasetAuditStatus status,
    long totalExampleCount,
    int regionCount,
    int categoryCount,
    String firstFeaturePeriod,
    String lastFeaturePeriod,
    int distinctFeaturePeriodCount,
    int longestConsecutiveQuarterCount,
    Map<DatasetSplit, Long> splitCounts,
    Map<String, ModelDatasetLabelAudit> labels,
    Map<String, ModelDatasetFeatureAudit> features,
    List<String> blockers,
    List<String> warnings,
    Instant generatedAt
) {
}
