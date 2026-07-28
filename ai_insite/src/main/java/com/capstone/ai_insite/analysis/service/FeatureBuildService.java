package com.capstone.ai_insite.analysis.service;

import com.capstone.ai_insite.analysis.entity.ModelFeatureSnapshotEntity;
import com.capstone.ai_insite.analysis.repository.ModelFeatureSnapshotJpaRepository;
import com.capstone.ai_insite.common.exception.ResourceNotFoundException;
import com.capstone.ai_insite.metric.domain.CommercialMetric;
import com.capstone.ai_insite.metric.entity.CommercialMetricSnapshotEntity;
import com.capstone.ai_insite.metric.entity.CommercialCompetitionFeatureEntity;
import com.capstone.ai_insite.metric.repository.CommercialCompetitionFeatureJpaRepository;
import com.capstone.ai_insite.metric.repository.CommercialMetricSnapshotJpaRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class FeatureBuildService {

    public static final String FEATURE_VERSION = "rule-v2-competition";

    private final CommercialMetricSnapshotJpaRepository metricRepository;
    private final CommercialCompetitionFeatureJpaRepository competitionRepository;
    private final ModelFeatureSnapshotJpaRepository featureRepository;
    private final ObjectMapper objectMapper;

    public FeatureBuildService(
        CommercialMetricSnapshotJpaRepository metricRepository,
        CommercialCompetitionFeatureJpaRepository competitionRepository,
        ModelFeatureSnapshotJpaRepository featureRepository,
        ObjectMapper objectMapper
    ) {
        this.metricRepository = metricRepository;
        this.competitionRepository = competitionRepository;
        this.featureRepository = featureRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ModelFeatureSnapshotEntity build(CommercialMetric metric) {
        CommercialMetricSnapshotEntity source = metricRepository.findById(metric.snapshotId())
            .orElseThrow(() -> new ResourceNotFoundException("통합 지표 스냅샷을 찾을 수 없습니다."));
        return featureRepository
            .findByRegionIdAndBusinessCategoryIdAndMetricPeriodIdAndFeatureVersion(
                source.getRegion().getId(),
                source.getBusinessCategory().getId(),
                source.getMetricPeriod().getId(),
                FEATURE_VERSION
            )
            .orElseGet(() -> featureRepository.save(new ModelFeatureSnapshotEntity(
                source.getRegion(),
                source.getBusinessCategory(),
                source.getMetricPeriod(),
                serialize(features(metric, competition(source))),
                FEATURE_VERSION
            )));
    }

    private CommercialCompetitionFeatureEntity competition(
        CommercialMetricSnapshotEntity source
    ) {
        return competitionRepository
            .findFirstByRegionIdAndBusinessCategoryIdAndMetricPeriodIdOrderBySnapshotDateDesc(
                source.getRegion().getId(),
                source.getBusinessCategory().getId(),
                source.getMetricPeriod().getId()
            )
            .orElse(null);
    }

    private static Map<String, Object> features(
        CommercialMetric metric,
        CommercialCompetitionFeatureEntity competition
    ) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("sourceMetricSnapshotId", metric.snapshotId());
        values.put("salesAmount", metric.sales().salesAmount());
        values.put("salesCount", metric.sales().salesCount());
        values.put("salesGrowthRateQoq", metric.sales().growthRateQoq());
        values.put("storeCount", metric.stores().storeCount());
        values.put("storeGrowthRateQoq", metric.stores().growthRateQoq());
        values.put("floatingPopulation", metric.demand().floatingPopulation());
        values.put("residentPopulation", metric.demand().residentPopulation());
        values.put("workingPopulation", metric.demand().workingPopulation());
        values.put("demandScore", metric.scores().demandScore());
        values.put("competitionScore", metric.scores().competitionScore());
        values.put("marketScore", metric.scores().marketScore());
        values.put("stabilityScore", metric.scores().stabilityScore());
        values.put("closureRiskSignal", metric.scores().closureRiskSignal());
        values.put(
            "activeStoreCount",
            competition == null ? null : competition.getActiveStoreCount()
        );
        values.put(
            "sameCategoryStoreCount",
            competition == null ? null : competition.getSameCategoryStoreCount()
        );
        values.put(
            "storeCountPerSquareKm",
            competition == null ? null : competition.getStoreCountPerSquareKm()
        );
        values.put(
            "franchiseStoreCountFromRegistry",
            competition == null ? null : competition.getFranchiseStoreCount()
        );
        values.put(
            "categoryDiversityIndex",
            competition == null ? null : competition.getCategoryDiversityIndex()
        );
        values.put(
            "competitionSnapshotDate",
            competition == null ? null : competition.getSnapshotDate()
        );
        values.put(
            "registeredVsSeoulStoreCountDifference",
            competition == null || metric.stores().storeCount() == null
                ? null
                : competition.getSameCategoryStoreCount()
                    - metric.stores().storeCount()
        );
        return values;
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("모델 피처 JSON 생성에 실패했습니다.", exception);
        }
    }
}
