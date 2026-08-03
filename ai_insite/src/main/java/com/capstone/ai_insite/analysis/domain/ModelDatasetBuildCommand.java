package com.capstone.ai_insite.analysis.domain;

public record ModelDatasetBuildCommand(
    String datasetVersion,
    String featureFromPeriod,
    String trainThroughPeriod,
    String validationThroughPeriod,
    String testThroughPeriod
) {

    public ModelDatasetBuildCommand {
        datasetVersion = required(datasetVersion, "datasetVersion");
        featureFromPeriod = required(featureFromPeriod, "featureFromPeriod");
        trainThroughPeriod = required(trainThroughPeriod, "trainThroughPeriod");
        validationThroughPeriod = required(
            validationThroughPeriod,
            "validationThroughPeriod"
        );
        testThroughPeriod = required(testThroughPeriod, "testThroughPeriod");
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + "는 필수입니다.");
        }
        return value.trim();
    }
}
