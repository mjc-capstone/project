package com.capstone.ai_insite.analysis.domain;

public record FeatureLabelBuildResult(
    String featureVersion,
    String labelVersion,
    int processedCount,
    int readyCount,
    int missingTargetCount,
    int incompleteSourceCount
) {
}
