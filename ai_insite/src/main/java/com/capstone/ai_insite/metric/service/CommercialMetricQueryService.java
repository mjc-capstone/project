package com.capstone.ai_insite.metric.service;

import com.capstone.ai_insite.common.exception.ResourceNotFoundException;
import com.capstone.ai_insite.metric.domain.CommercialMetric;
import com.capstone.ai_insite.metric.entity.CommercialMetricSnapshotEntity;
import com.capstone.ai_insite.metric.entity.MetricPeriodEntity;
import com.capstone.ai_insite.metric.entity.RegionPeriodFeatureEntity;
import com.capstone.ai_insite.metric.repository.CommercialMetricSnapshotJpaRepository;
import com.capstone.ai_insite.metric.repository.MetricPeriodJpaRepository;
import com.capstone.ai_insite.metric.repository.RegionPeriodFeatureJpaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CommercialMetricQueryService {

    private final CommercialMetricSnapshotJpaRepository snapshotRepository;
    private final RegionPeriodFeatureJpaRepository featureRepository;
    private final MetricPeriodJpaRepository periodRepository;

    public CommercialMetricQueryService(
        CommercialMetricSnapshotJpaRepository snapshotRepository,
        RegionPeriodFeatureJpaRepository featureRepository,
        MetricPeriodJpaRepository periodRepository
    ) {
        this.snapshotRepository = snapshotRepository;
        this.featureRepository = featureRepository;
        this.periodRepository = periodRepository;
    }

    public CommercialMetric getSummary(
        String regionCode,
        String categoryCode,
        String periodCode
    ) {
        CommercialMetricSnapshotEntity snapshot = snapshotRepository
            .findByRegionAdministrativeDongCodeAndBusinessCategorySourceCategoryCodeAndMetricPeriodPeriodCode(
                regionCode,
                categoryCode,
                periodCode
            )
            .orElseThrow(() -> new ResourceNotFoundException("통합 지표를 찾을 수 없습니다."));
        return CommercialMetricMapper.toDomain(snapshot, feature(snapshot));
    }

    public List<CommercialMetric> getTrends(
        String regionCode,
        String categoryCode,
        String fromPeriod,
        String toPeriod
    ) {
        MetricPeriodEntity from = period(fromPeriod);
        MetricPeriodEntity to = period(toPeriod);
        return snapshotRepository
            .findByRegionAdministrativeDongCodeAndBusinessCategorySourceCategoryCodeAndMetricPeriodStartDateBetweenOrderByMetricPeriodStartDate(
                regionCode,
                categoryCode,
                from.getStartDate(),
                to.getEndDate()
            )
            .stream()
            .map(snapshot -> CommercialMetricMapper.toDomain(snapshot, feature(snapshot)))
            .toList();
    }

    private RegionPeriodFeatureEntity feature(CommercialMetricSnapshotEntity snapshot) {
        return featureRepository
            .findByRegionIdAndMetricPeriodId(
                snapshot.getRegion().getId(),
                snapshot.getMetricPeriod().getId()
            )
            .orElse(null);
    }

    private MetricPeriodEntity period(String code) {
        return periodRepository.findByPeriodCode(code)
            .orElseThrow(() -> new ResourceNotFoundException("지표 기간을 찾을 수 없습니다: " + code));
    }
}
