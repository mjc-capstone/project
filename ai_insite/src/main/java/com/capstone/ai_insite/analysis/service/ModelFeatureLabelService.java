package com.capstone.ai_insite.analysis.service;

import com.capstone.ai_insite.analysis.domain.FeatureLabelBuildResult;
import com.capstone.ai_insite.analysis.repository.ModelFeatureLabelJdbcRepository;
import com.capstone.ai_insite.analysis.domain.ModelLabelDecision;
import com.capstone.ai_insite.analysis.domain.ModelLabelObservation;
import com.capstone.ai_insite.analysis.domain.ModelLabelStatus;
import com.capstone.ai_insite.analysis.domain.policy.NextQuarterLabelPolicy;
import com.capstone.ai_insite.analysis.entity.ModelFeatureSnapshotEntity;
import com.capstone.ai_insite.metric.domain.CommercialMetric;
import com.capstone.ai_insite.metric.entity.CommercialMetricSnapshotEntity;
import com.capstone.ai_insite.metric.repository.CommercialMetricSnapshotJpaRepository;
import com.capstone.ai_insite.metric.repository.MetricPeriodJpaRepository;
import com.capstone.ai_insite.metric.service.CommercialMetricQueryService;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class ModelFeatureLabelService {

    public static final String LABEL_VERSION = "label-v1-next-quarter";

    private final CommercialMetricQueryService metricQueryService;
    private final CommercialMetricSnapshotJpaRepository metricRepository;
    private final FeatureBuildService featureBuildService;
    private final NextQuarterLabelPolicy labelPolicy;
    private final ObjectMapper objectMapper;
    private final ModelFeatureLabelJdbcRepository bulkRepository;
    private final MetricPeriodJpaRepository periodRepository;

    public ModelFeatureLabelService(
        CommercialMetricQueryService metricQueryService,
        CommercialMetricSnapshotJpaRepository metricRepository,
        FeatureBuildService featureBuildService,
        NextQuarterLabelPolicy labelPolicy,
        ObjectMapper objectMapper,
        ModelFeatureLabelJdbcRepository bulkRepository,
        MetricPeriodJpaRepository periodRepository
    ) {
        this.metricQueryService = metricQueryService;
        this.metricRepository = metricRepository;
        this.featureBuildService = featureBuildService;
        this.labelPolicy = labelPolicy;
        this.objectMapper = objectMapper;
        this.bulkRepository = bulkRepository;
        this.periodRepository = periodRepository;
    }

    public FeatureLabelBuildResult rebuild(String fromPeriod, String toPeriod) {
        var from = periodRepository.findByPeriodCode(fromPeriod)
            .orElseThrow(() -> new IllegalArgumentException(
                "지표 기간을 찾을 수 없습니다: " + fromPeriod
            ));
        var to = periodRepository.findByPeriodCode(toPeriod)
            .orElseThrow(() -> new IllegalArgumentException(
                "지표 기간을 찾을 수 없습니다: " + toPeriod
            ));
        if (from.getStartDate().isAfter(to.getEndDate())) {
            throw new IllegalArgumentException("조회 시작 분기는 종료 분기보다 늦을 수 없습니다.");
        }
        return bulkRepository.rebuild(from.getStartDate(), to.getEndDate());
    }

    @Transactional
    FeatureLabelBuildResult rebuildOneByOne(String fromPeriod, String toPeriod) {
        int ready = 0;
        int missing = 0;
        int incomplete = 0;
        var metrics = metricQueryService.getAll(fromPeriod, toPeriod);

        for (CommercialMetric metric : metrics) {
            ModelFeatureSnapshotEntity feature = featureBuildService.build(metric);
            CommercialMetricSnapshotEntity current = metricRepository
                .findById(metric.snapshotId())
                .orElseThrow();
            Optional<CommercialMetricSnapshotEntity> next = metricAt(
                current,
                current.getMetricPeriod().getStartDate().plusMonths(3)
            );
            if (next.isEmpty()) {
                feature.markLabelUnavailable(
                    ModelLabelStatus.MISSING_TARGET,
                    null,
                    LABEL_VERSION
                );
                missing++;
                continue;
            }

            Optional<CommercialMetricSnapshotEntity> fourQuartersLater = metricAt(
                current,
                current.getMetricPeriod().getStartDate().plusMonths(12)
            );
            ModelLabelDecision decision = labelPolicy.calculate(
                observation(current),
                observation(next.get()),
                fourQuartersLater.map(ModelFeatureLabelService::observation).orElse(null)
            );
            if (decision.status() == ModelLabelStatus.READY) {
                feature.applyReadyLabel(
                    next.get().getMetricPeriod(),
                    fourQuartersLater
                        .map(CommercialMetricSnapshotEntity::getMetricPeriod)
                        .orElse(next.get().getMetricPeriod()),
                    serialize(decision.values()),
                    LABEL_VERSION
                );
                ready++;
            } else {
                feature.markLabelUnavailable(
                    ModelLabelStatus.INCOMPLETE_SOURCE,
                    next.get().getMetricPeriod(),
                    LABEL_VERSION
                );
                incomplete++;
            }
        }

        return new FeatureLabelBuildResult(
            FeatureBuildService.FEATURE_VERSION,
            LABEL_VERSION,
            metrics.size(),
            ready,
            missing,
            incomplete
        );
    }

    private Optional<CommercialMetricSnapshotEntity> metricAt(
        CommercialMetricSnapshotEntity source,
        java.time.LocalDate startDate
    ) {
        return metricRepository
            .findByRegionIdAndBusinessCategoryIdAndMetricPeriodStartDate(
                source.getRegion().getId(),
                source.getBusinessCategory().getId(),
                startDate
            );
    }

    private static ModelLabelObservation observation(
        CommercialMetricSnapshotEntity source
    ) {
        return new ModelLabelObservation(
            source.getMetricPeriod().getPeriodCode(),
            source.getMetricPeriod().getStartDate(),
            source.getSalesAmount(),
            source.getStoreCount(),
            source.getCloseRate()
        );
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("모델 라벨 JSON 생성에 실패했습니다.", exception);
        }
    }
}
