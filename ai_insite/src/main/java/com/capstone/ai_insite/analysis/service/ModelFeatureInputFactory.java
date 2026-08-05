package com.capstone.ai_insite.analysis.service;

import com.capstone.ai_insite.analysis.client.PredictionModelProperties;
import com.capstone.ai_insite.analysis.domain.MarketFeatureVector;
import com.capstone.ai_insite.analysis.domain.ModelFeatureInput;
import com.capstone.ai_insite.analysis.domain.UserConditionVector;
import com.capstone.ai_insite.analysis.repository.ModelFeatureSnapshotJpaRepository;
import com.capstone.ai_insite.common.exception.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class ModelFeatureInputFactory {

    private final ModelFeatureSnapshotJpaRepository featureRepository;
    private final PredictionModelProperties properties;
    private final ObjectMapper objectMapper;

    public ModelFeatureInputFactory(
        ModelFeatureSnapshotJpaRepository featureRepository,
        PredictionModelProperties properties,
        ObjectMapper objectMapper
    ) {
        this.featureRepository = featureRepository;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public ModelFeatureInput create(Long featureSnapshotId) {
        var feature = featureRepository.findById(featureSnapshotId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "모델 피처 스냅샷을 찾을 수 없습니다: " + featureSnapshotId
            ));
        if (properties.getReleaseVersion().isBlank()) {
            throw new IllegalStateException("요청할 모델 릴리스가 설정되지 않았습니다.");
        }
        try {
            JsonNode values = objectMapper.readTree(feature.getFeatureJson());
            MarketFeatureVector market = new MarketFeatureVector(
                feature.getBusinessCategory().getSourceCategoryCode(),
                feature.getRegion().getAdministrativeDongCode(),
                number(values, "salesAmount"),
                number(values, "salesCount"),
                number(values, "salesGrowthRateQoq"),
                number(values, "storeCount"),
                number(values, "storeGrowthRateQoq"),
                number(values, "demandScore"),
                number(values, "competitionScore"),
                number(values, "marketScore"),
                number(values, "stabilityScore"),
                number(values, "closureRiskSignal"),
                number(values, "floatingPopulation"),
                number(values, "residentPopulation"),
                number(values, "workingPopulation")
            );
            return new ModelFeatureInput(
                UUID.randomUUID().toString(),
                properties.getReleaseVersion(),
                feature.getFeatureVersion(),
                feature.getId(),
                feature.getFeatureAsOfDate(),
                market,
                UserConditionVector.empty()
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                "모델 피처 JSON을 입력 계약으로 변환하지 못했습니다.",
                exception
            );
        }
    }

    private static Double number(JsonNode values, String name) {
        JsonNode value = values.get(name);
        return value == null || value.isNull() ? null : value.asDouble();
    }
}
