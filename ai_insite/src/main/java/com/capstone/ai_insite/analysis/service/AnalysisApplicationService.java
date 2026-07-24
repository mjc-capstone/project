package com.capstone.ai_insite.analysis.service;

import com.capstone.ai_insite.analysis.domain.AnalysisCommand;
import com.capstone.ai_insite.analysis.domain.AnalysisPrediction;
import com.capstone.ai_insite.analysis.domain.AnalysisResult;
import com.capstone.ai_insite.analysis.domain.RecommendationGrade;
import com.capstone.ai_insite.analysis.domain.policy.RecommendationPolicy;
import com.capstone.ai_insite.analysis.domain.policy.RiskPredictionPolicy;
import com.capstone.ai_insite.analysis.entity.AnalysisRequestEntity;
import com.capstone.ai_insite.analysis.entity.AnalysisResultEntity;
import com.capstone.ai_insite.analysis.entity.ModelFeatureSnapshotEntity;
import com.capstone.ai_insite.analysis.repository.AnalysisRequestJpaRepository;
import com.capstone.ai_insite.analysis.repository.AnalysisResultJpaRepository;
import com.capstone.ai_insite.category.entity.BusinessCategoryEntity;
import com.capstone.ai_insite.category.repository.BusinessCategoryJpaRepository;
import com.capstone.ai_insite.common.exception.ResourceNotFoundException;
import com.capstone.ai_insite.metric.domain.CommercialMetric;
import com.capstone.ai_insite.metric.service.CommercialMetricQueryService;
import com.capstone.ai_insite.region.entity.RegionEntity;
import com.capstone.ai_insite.region.repository.RegionJpaRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class AnalysisApplicationService {

    private final RegionJpaRepository regionRepository;
    private final BusinessCategoryJpaRepository categoryRepository;
    private final AnalysisRequestJpaRepository requestRepository;
    private final AnalysisResultJpaRepository resultRepository;
    private final CommercialMetricQueryService metricQueryService;
    private final FeatureBuildService featureBuildService;
    private final RiskPredictionPolicy riskPredictionPolicy;
    private final RecommendationPolicy recommendationPolicy;
    private final ObjectMapper objectMapper;

    public AnalysisApplicationService(
        RegionJpaRepository regionRepository,
        BusinessCategoryJpaRepository categoryRepository,
        AnalysisRequestJpaRepository requestRepository,
        AnalysisResultJpaRepository resultRepository,
        CommercialMetricQueryService metricQueryService,
        FeatureBuildService featureBuildService,
        RiskPredictionPolicy riskPredictionPolicy,
        RecommendationPolicy recommendationPolicy,
        ObjectMapper objectMapper
    ) {
        this.regionRepository = regionRepository;
        this.categoryRepository = categoryRepository;
        this.requestRepository = requestRepository;
        this.resultRepository = resultRepository;
        this.metricQueryService = metricQueryService;
        this.featureBuildService = featureBuildService;
        this.riskPredictionPolicy = riskPredictionPolicy;
        this.recommendationPolicy = recommendationPolicy;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AnalysisResult analyze(AnalysisCommand command) {
        RegionEntity region = regionRepository
            .findByAdministrativeDongCodeAndActiveTrue(command.regionCode())
            .orElseThrow(() -> new ResourceNotFoundException(
                "행정동을 찾을 수 없습니다: " + command.regionCode()
            ));
        BusinessCategoryEntity category = categoryRepository
            .findBySourceCategoryCodeAndActiveTrue(command.categoryCode())
            .orElseThrow(() -> new ResourceNotFoundException(
                "업종을 찾을 수 없습니다: " + command.categoryCode()
            ));
        CommercialMetric metric = metricQueryService.getSummary(
            command.regionCode(),
            command.categoryCode(),
            command.periodCode()
        );
        AnalysisRequestEntity request = requestRepository.save(
            new AnalysisRequestEntity(region, category, command)
        );
        ModelFeatureSnapshotEntity feature = featureBuildService.build(metric);
        AnalysisPrediction prediction = riskPredictionPolicy.predict(metric, command.condition());
        RecommendationGrade grade = recommendationPolicy.grade(prediction);
        String summary = summary(grade, prediction);

        Map<String, Object> modelOutput = new LinkedHashMap<>();
        modelOutput.put("engine", FeatureBuildService.FEATURE_VERSION);
        modelOutput.put("successScore", prediction.successScore());
        modelOutput.put("closureRiskScore", prediction.closureRiskScore());
        modelOutput.put("locationFitScore", prediction.locationFitScore());

        AnalysisResultEntity saved = resultRepository.save(new AnalysisResultEntity(
            request,
            feature,
            prediction,
            grade,
            serialize(prediction.positiveFactors()),
            serialize(prediction.riskFactors()),
            serialize(modelOutput),
            summary
        ));
        return toDomain(saved);
    }

    @Transactional(readOnly = true)
    public AnalysisResult get(Long analysisId) {
        return resultRepository.findById(analysisId)
            .map(this::toDomain)
            .orElseThrow(() -> new ResourceNotFoundException(
                "분석 결과를 찾을 수 없습니다: " + analysisId
            ));
    }

    private AnalysisResult toDomain(AnalysisResultEntity entity) {
        return new AnalysisResult(
            entity.getId(),
            entity.getAnalysisRequest().getRegion().getAdministrativeDongCode(),
            entity.getAnalysisRequest().getBusinessCategory().getSourceCategoryCode(),
            entity.getModelFeatureSnapshot().getMetricPeriod().getPeriodCode(),
            entity.getSuccessScore(),
            entity.getClosureRiskScore(),
            entity.getLocationFitScore(),
            RecommendationGrade.valueOf(entity.getRecommendationGrade()),
            parseList(entity.getPositiveFactorsJson()),
            parseList(entity.getRiskFactorsJson()),
            entity.getAiSummary(),
            entity.getModelFeatureSnapshot().getFeatureVersion(),
            entity.getCreatedAt()
        );
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("분석 결과 JSON 생성에 실패했습니다.", exception);
        }
    }

    private List<String> parseList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            List<String> values = new ArrayList<>();
            root.forEach(node -> values.add(node.asString()));
            return List.copyOf(values);
        } catch (Exception exception) {
            throw new IllegalStateException("분석 결과 JSON 해석에 실패했습니다.", exception);
        }
    }

    private static String summary(
        RecommendationGrade grade,
        AnalysisPrediction prediction
    ) {
        return "추천 등급 " + grade
            + ", 성공 가능성 " + prediction.successScore().stripTrailingZeros().toPlainString()
            + "점, 폐업 위험 " + prediction.closureRiskScore().stripTrailingZeros().toPlainString()
            + "점으로 산출되었습니다.";
    }
}
