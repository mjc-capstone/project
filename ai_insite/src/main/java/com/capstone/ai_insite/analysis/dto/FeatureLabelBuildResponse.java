package com.capstone.ai_insite.analysis.dto;

import com.capstone.ai_insite.analysis.domain.FeatureLabelBuildResult;

public record FeatureLabelBuildResponse(
    String featureVersion,
    String labelVersion,
    int processedCount,
    int readyCount,
    int missingTargetCount,
    int incompleteSourceCount
) {

    public static FeatureLabelBuildResponse from(FeatureLabelBuildResult result) {
        return new FeatureLabelBuildResponse(
            result.featureVersion(),
            result.labelVersion(),
            result.processedCount(),
            result.readyCount(),
            result.missingTargetCount(),
            result.incompleteSourceCount()
        );
    }
}
