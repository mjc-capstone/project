package com.capstone.ai_insite.analysis.dto;

import com.capstone.ai_insite.analysis.domain.ModelDatasetBuildCommand;

public record ModelDatasetBuildRequest(
    String datasetVersion,
    String featureFromPeriod,
    String trainThroughPeriod,
    String validationThroughPeriod,
    String testThroughPeriod
) {

    public ModelDatasetBuildCommand toCommand() {
        return new ModelDatasetBuildCommand(
            datasetVersion,
            featureFromPeriod,
            trainThroughPeriod,
            validationThroughPeriod,
            testThroughPeriod
        );
    }
}
