package com.capstone.ai_insite.analysis.service;

import com.capstone.ai_insite.analysis.domain.DatasetSplit;
import com.capstone.ai_insite.analysis.domain.ModelDatasetBuildCommand;
import com.capstone.ai_insite.analysis.domain.ModelDatasetBuildResult;
import com.capstone.ai_insite.analysis.domain.ModelDatasetExample;
import com.capstone.ai_insite.analysis.domain.ModelEvaluationCommand;
import com.capstone.ai_insite.analysis.domain.policy.TemporalDatasetSplitPolicy;
import com.capstone.ai_insite.analysis.entity.ModelDatasetBuildEntity;
import com.capstone.ai_insite.analysis.entity.ModelDatasetMemberEntity;
import com.capstone.ai_insite.analysis.entity.ModelFeatureSnapshotEntity;
import com.capstone.ai_insite.analysis.repository.ModelDatasetBuildJpaRepository;
import com.capstone.ai_insite.analysis.repository.ModelDatasetMemberJpaRepository;
import com.capstone.ai_insite.analysis.repository.ModelDatasetMemberJdbcRepository;
import com.capstone.ai_insite.common.exception.ResourceNotFoundException;
import com.capstone.ai_insite.metric.entity.MetricPeriodEntity;
import com.capstone.ai_insite.metric.repository.MetricPeriodJpaRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModelDatasetApplicationService {

    private final ModelFeatureLabelService featureLabelService;
    private final TemporalDatasetSplitPolicy splitPolicy;
    private final MetricPeriodJpaRepository periodRepository;
    private final ModelDatasetBuildJpaRepository datasetRepository;
    private final ModelDatasetMemberJpaRepository memberRepository;
    private final ModelDatasetMemberJdbcRepository memberJdbcRepository;

    public ModelDatasetApplicationService(
        ModelFeatureLabelService featureLabelService,
        TemporalDatasetSplitPolicy splitPolicy,
        MetricPeriodJpaRepository periodRepository,
        ModelDatasetBuildJpaRepository datasetRepository,
        ModelDatasetMemberJpaRepository memberRepository,
        ModelDatasetMemberJdbcRepository memberJdbcRepository
    ) {
        this.featureLabelService = featureLabelService;
        this.splitPolicy = splitPolicy;
        this.periodRepository = periodRepository;
        this.datasetRepository = datasetRepository;
        this.memberRepository = memberRepository;
        this.memberJdbcRepository = memberJdbcRepository;
    }

    @Transactional
    public ModelDatasetBuildResult build(ModelDatasetBuildCommand command) {
        return build(command, FeatureBuildService.FEATURE_VERSION);
    }

    @Transactional
    public ModelDatasetBuildResult build(
        ModelDatasetBuildCommand command,
        String featureVersion
    ) {
        if (datasetRepository.existsByDatasetVersion(command.datasetVersion())) {
            throw new IllegalArgumentException(
                "이미 존재하는 데이터셋 버전입니다: " + command.datasetVersion()
            );
        }
        MetricPeriodEntity featureFrom = period(command.featureFromPeriod());
        MetricPeriodEntity trainThrough = period(command.trainThroughPeriod());
        MetricPeriodEntity validationThrough = period(command.validationThroughPeriod());
        MetricPeriodEntity testThrough = period(command.testThroughPeriod());
        requireQuarter(featureFrom, trainThrough, validationThrough, testThrough);
        splitPolicy.validateBoundaries(
            trainThrough.getEndDate(),
            validationThrough.getEndDate(),
            testThrough.getEndDate()
        );
        if (featureFrom.getStartDate().isAfter(trainThrough.getEndDate())) {
            throw new IllegalArgumentException("피처 시작 분기는 Train 종료 분기보다 늦을 수 없습니다.");
        }

        featureLabelService.rebuild(
            featureFrom.getPeriodCode(),
            testThrough.getPeriodCode(),
            featureVersion
        );
        ModelDatasetBuildEntity dataset = datasetRepository.save(
            new ModelDatasetBuildEntity(
                command.datasetVersion(),
                featureVersion,
                ModelFeatureLabelService.LABEL_VERSION,
                featureFrom,
                trainThrough,
                validationThrough,
                testThrough
            )
        );

        Map<DatasetSplit, Integer> counts = memberJdbcRepository.createMembers(
            dataset.getId(),
            featureVersion,
            ModelFeatureLabelService.LABEL_VERSION,
            featureFrom.getStartDate(),
            testThrough.getEndDate(),
            trainThrough.getEndDate(),
            validationThrough.getEndDate(),
            testThrough.getEndDate()
        );
        dataset.complete(
            counts.getOrDefault(DatasetSplit.TRAIN, 0),
            counts.getOrDefault(DatasetSplit.VALIDATION, 0),
            counts.getOrDefault(DatasetSplit.TEST, 0)
        );
        return result(dataset);
    }

    @Transactional(readOnly = true)
    public ModelDatasetBuildResult get(Long datasetId) {
        return result(dataset(datasetId));
    }

    @Transactional
    public ModelDatasetBuildResult recordEvaluation(
        Long datasetId,
        ModelEvaluationCommand command
    ) {
        ModelDatasetBuildEntity dataset = dataset(datasetId);
        dataset.recordEvaluation(
            command.modelVersion(),
            command.evaluationMetricsJson()
        );
        return result(dataset);
    }

    @Transactional(readOnly = true)
    public List<ModelDatasetExample> getExamples(
        Long datasetId,
        DatasetSplit split
    ) {
        dataset(datasetId);
        List<ModelDatasetMemberEntity> members = split == null
            ? memberRepository
                .findByDatasetBuildIdOrderByFeatureSnapshotMetricPeriodStartDateAsc(datasetId)
            : memberRepository
                .findByDatasetBuildIdAndDatasetSplitOrderByFeatureSnapshotMetricPeriodStartDateAsc(
                    datasetId,
                    split
                );
        return members.stream().map(ModelDatasetApplicationService::example).toList();
    }

    private ModelDatasetBuildEntity dataset(Long id) {
        return datasetRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "모델 데이터셋을 찾을 수 없습니다: " + id
            ));
    }

    private MetricPeriodEntity period(String code) {
        return periodRepository.findByPeriodCode(code)
            .orElseThrow(() -> new ResourceNotFoundException(
                "지표 기간을 찾을 수 없습니다: " + code
            ));
    }

    private static void requireQuarter(MetricPeriodEntity... periods) {
        for (MetricPeriodEntity period : periods) {
            if (!"QUARTER".equals(period.getPeriodType())) {
                throw new IllegalArgumentException("모델 데이터셋은 분기 지표만 지원합니다.");
            }
        }
    }

    private static ModelDatasetBuildResult result(ModelDatasetBuildEntity entity) {
        return new ModelDatasetBuildResult(
            entity.getId(),
            entity.getDatasetVersion(),
            entity.getFeatureVersion(),
            entity.getLabelVersion(),
            entity.getFeatureFromPeriod().getPeriodCode(),
            entity.getTrainThroughPeriod().getPeriodCode(),
            entity.getValidationThroughPeriod().getPeriodCode(),
            entity.getTestThroughPeriod().getPeriodCode(),
            entity.getStatus(),
            entity.getEligibleFeatureCount(),
            entity.getTrainExampleCount(),
            entity.getValidationExampleCount(),
            entity.getTestExampleCount(),
            entity.getModelVersion(),
            entity.getEvaluationMetricsJson(),
            entity.getCompletedAt(),
            entity.getCreatedAt()
        );
    }

    private static ModelDatasetExample example(ModelDatasetMemberEntity member) {
        ModelFeatureSnapshotEntity feature = member.getFeatureSnapshot();
        return new ModelDatasetExample(
            feature.getId(),
            feature.getRegion().getAdministrativeDongCode(),
            feature.getBusinessCategory().getSourceCategoryCode(),
            feature.getMetricPeriod().getPeriodCode(),
            feature.getFeatureAsOfDate(),
            feature.getLabelPeriod().getPeriodCode(),
            feature.getLabelHorizonPeriod().getPeriodCode(),
            member.getDatasetSplit(),
            feature.getFeatureVersion(),
            feature.getLabelVersion(),
            feature.getFeatureJson(),
            feature.getLabelJson()
        );
    }
}
