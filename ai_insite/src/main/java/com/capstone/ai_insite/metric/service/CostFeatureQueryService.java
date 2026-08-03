package com.capstone.ai_insite.metric.service;

import com.capstone.ai_insite.metric.domain.CostFeatureContext;
import com.capstone.ai_insite.metric.entity.RegionCostFeatureEntity;
import com.capstone.ai_insite.metric.repository.MetricPeriodJpaRepository;
import com.capstone.ai_insite.metric.repository.RegionCostFeatureJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CostFeatureQueryService {

    private final RegionCostFeatureJpaRepository repository;
    private final MetricPeriodJpaRepository periodRepository;

    public CostFeatureQueryService(
        RegionCostFeatureJpaRepository repository,
        MetricPeriodJpaRepository periodRepository
    ) {
        this.repository = repository;
        this.periodRepository = periodRepository;
    }

    @Transactional(readOnly = true)
    public CostFeatureContext find(Long regionId, String periodCode) {
        return periodRepository.findByPeriodCode(periodCode)
            .map(period -> find(regionId, period.getId()))
            .orElseGet(CostFeatureContext::empty);
    }

    @Transactional(readOnly = true)
    public CostFeatureContext find(Long regionId, Long metricPeriodId) {
        RegionCostFeatureEntity transaction = repository
            .findByRegionIdAndMetricPeriodIdAndSourceSystemAndPropertyType(
                regionId,
                metricPeriodId,
                CommercialTransactionCostFeatureAggregationService.SOURCE_SYSTEM,
                CommercialTransactionCostFeatureAggregationService.PROPERTY_TYPE
            )
            .orElse(null);
        RegionCostFeatureEntity rent = repository
            .findFirstByMetricPeriodIdAndSourceSystemAndSourceRegionNameAndRegionLevelAndPropertyType(
                metricPeriodId,
                RebCostFeatureAggregationService.SOURCE_SYSTEM,
                "서울",
                "SIDO",
                "SMALL_RETAIL"
            )
            .orElse(null);
        if (transaction == null && rent == null) {
            return CostFeatureContext.empty();
        }
        return new CostFeatureContext(
            rent == null ? null : rent.getRentAmount(),
            rent == null ? null : rent.getRentIndex(),
            rent == null ? null : rent.getVacancyRate(),
            rent == null ? null : rent.getInvestmentReturnRate(),
            rent == null ? null : rent.getRentPressureScore(),
            rent == null ? null : rent.getVacancyRiskScore(),
            rent == null ? null : rent.getFixedCostBurdenIndex(),
            transaction == null ? null : transaction.getCommercialTransactionCount(),
            transaction == null ? null : transaction.getMedianCommercialPricePerArea(),
            transaction == null ? null : transaction.getPriceGrowthRate(),
            transaction == null ? null : transaction.getLocationCostScore()
        );
    }
}
