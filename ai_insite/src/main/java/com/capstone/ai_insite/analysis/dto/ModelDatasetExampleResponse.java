package com.capstone.ai_insite.analysis.dto;

import com.capstone.ai_insite.analysis.domain.DatasetSplit;
import com.capstone.ai_insite.analysis.domain.ModelDatasetExample;
import java.time.LocalDate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public record ModelDatasetExampleResponse(
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
    JsonNode features,
    JsonNode labels
) {

    public static ModelDatasetExampleResponse from(
        ModelDatasetExample example,
        ObjectMapper objectMapper
    ) {
        try {
            return new ModelDatasetExampleResponse(
                example.featureSnapshotId(),
                example.regionCode(),
                example.categoryCode(),
                example.featurePeriod(),
                example.featureAsOfDate(),
                example.labelPeriod(),
                example.labelHorizonPeriod(),
                example.split(),
                example.featureVersion(),
                example.labelVersion(),
                objectMapper.readTree(example.featureJson()),
                objectMapper.readTree(example.labelJson())
            );
        } catch (Exception exception) {
            throw new IllegalStateException("모델 데이터셋 JSON 해석에 실패했습니다.", exception);
        }
    }
}
