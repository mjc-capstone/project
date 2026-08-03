package com.capstone.ai_insite.analysis.service;

import com.capstone.ai_insite.analysis.entity.ModelFeatureSnapshotEntity;
import com.capstone.ai_insite.analysis.repository.ModelFeatureSnapshotJpaRepository;
import com.capstone.ai_insite.common.exception.ResourceNotFoundException;
import com.capstone.ai_insite.metric.domain.CommercialMetric;
import com.capstone.ai_insite.metric.domain.BuildingFeatureContext;
import com.capstone.ai_insite.metric.domain.CostFeatureContext;
import com.capstone.ai_insite.metric.entity.CommercialMetricSnapshotEntity;
import com.capstone.ai_insite.metric.entity.CommercialCompetitionFeatureEntity;
import com.capstone.ai_insite.metric.repository.CommercialCompetitionFeatureJpaRepository;
import com.capstone.ai_insite.metric.repository.CommercialMetricSnapshotJpaRepository;
import com.capstone.ai_insite.metric.service.CostFeatureQueryService;
import com.capstone.ai_insite.metric.service.BuildingFeatureQueryService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class FeatureBuildService {

    public static final String FEATURE_VERSION = "feature-v3-building";

    private final CommercialMetricSnapshotJpaRepository metricRepository;
    private final CommercialCompetitionFeatureJpaRepository competitionRepository;
    private final ModelFeatureSnapshotJpaRepository featureRepository;
    private final CostFeatureQueryService costFeatureQueryService;
    private final BuildingFeatureQueryService buildingFeatureQueryService;
    private final ObjectMapper objectMapper;

    public FeatureBuildService(
        CommercialMetricSnapshotJpaRepository metricRepository,
        CommercialCompetitionFeatureJpaRepository competitionRepository,
        ModelFeatureSnapshotJpaRepository featureRepository,
        CostFeatureQueryService costFeatureQueryService,
        BuildingFeatureQueryService buildingFeatureQueryService,
        ObjectMapper objectMapper
    ) {
        this.metricRepository = metricRepository;
        this.competitionRepository = competitionRepository;
        this.featureRepository = featureRepository;
        this.costFeatureQueryService = costFeatureQueryService;
        this.buildingFeatureQueryService = buildingFeatureQueryService;
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
                serialize(features(
                    metric,
                    competition(source),
                    costFeatureQueryService.find(
                        source.getRegion().getId(),
                        source.getMetricPeriod().getId()
                    ),
                    buildingFeatureQueryService.find(
                        source.getRegion().getId(),
                        source.getMetricPeriod().getId()
                    )
                )),
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
        CommercialCompetitionFeatureEntity competition,
        CostFeatureContext cost,
        BuildingFeatureContext building
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
        values.put("rentAmountPerSquareMeter", cost.rentAmountPerSquareMeter());
        values.put("rentIndex", cost.rentIndex());
        values.put("vacancyRate", cost.vacancyRate());
        values.put("investmentReturnRate", cost.investmentReturnRate());
        values.put("rentPressureScore", cost.rentPressureScore());
        values.put("vacancyRiskScore", cost.vacancyRiskScore());
        values.put("fixedCostBurdenIndex", cost.fixedCostBurdenIndex());
        values.put("commercialTransactionCount", cost.commercialTransactionCount());
        values.put(
            "medianCommercialPricePerArea",
            cost.medianCommercialPricePerArea()
        );
        values.put("commercialPriceGrowthRate", cost.priceGrowthRate());
        values.put("locationCostScore", cost.locationCostScore());
        values.put("totalBuildingCount", building.totalBuildingCount());
        values.put("commercialBuildingCount", building.commercialBuildingCount());
        values.put("averageBuildingAge", building.averageBuildingAge());
        values.put("agedBuildingRatio", building.agedBuildingRatio());
        values.put("averageGrossFloorArea", building.averageGrossFloorArea());
        values.put("totalParkingCount", building.totalParkingCount());
        values.put(
            "parkingSpacesPerCommercialBuilding",
            building.parkingSpacesPerCommercialBuilding()
        );
        values.put(
            "commercialFloorAreaProxy",
            building.commercialFloorAreaProxy()
        );
        values.put(
            "commercialFloorAreaRatio",
            building.commercialFloorAreaRatio()
        );
        values.put(
            "physicalEnvironmentScore",
            building.physicalEnvironmentScore()
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
