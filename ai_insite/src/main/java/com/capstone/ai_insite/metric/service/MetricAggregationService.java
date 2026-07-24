package com.capstone.ai_insite.metric.service;

import com.capstone.ai_insite.category.entity.BusinessCategoryEntity;
import com.capstone.ai_insite.category.repository.BusinessCategoryJpaRepository;
import com.capstone.ai_insite.common.exception.ResourceNotFoundException;
import com.capstone.ai_insite.metric.domain.CommercialMetric;
import com.capstone.ai_insite.metric.domain.DemandMetric;
import com.capstone.ai_insite.metric.domain.SalesMetric;
import com.capstone.ai_insite.metric.domain.StoreMetric;
import com.capstone.ai_insite.metric.domain.policy.CompetitionScoreCalculator;
import com.capstone.ai_insite.metric.domain.policy.DemandScoreCalculator;
import com.capstone.ai_insite.metric.domain.policy.MarketScoreCalculator;
import com.capstone.ai_insite.metric.domain.policy.StabilityScoreCalculator;
import com.capstone.ai_insite.metric.entity.CommercialMetricSnapshotEntity;
import com.capstone.ai_insite.metric.entity.MetricPeriodEntity;
import com.capstone.ai_insite.metric.entity.RegionPeriodFeatureEntity;
import com.capstone.ai_insite.metric.entity.SourceSeoulSalesEntity;
import com.capstone.ai_insite.metric.entity.SourceSeoulStoresEntity;
import com.capstone.ai_insite.metric.repository.CommercialMetricSnapshotJpaRepository;
import com.capstone.ai_insite.metric.repository.MetricPeriodJpaRepository;
import com.capstone.ai_insite.metric.repository.RegionPeriodFeatureJpaRepository;
import com.capstone.ai_insite.metric.repository.SourceSeoulSalesJpaRepository;
import com.capstone.ai_insite.metric.repository.SourceSeoulStoresJpaRepository;
import com.capstone.ai_insite.region.entity.RegionEntity;
import com.capstone.ai_insite.region.repository.RegionJpaRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MetricAggregationService {

    private final RegionJpaRepository regionRepository;
    private final BusinessCategoryJpaRepository categoryRepository;
    private final MetricPeriodJpaRepository periodRepository;
    private final SourceSeoulSalesJpaRepository salesRepository;
    private final SourceSeoulStoresJpaRepository storesRepository;
    private final RegionPeriodFeatureJpaRepository featureRepository;
    private final CommercialMetricSnapshotJpaRepository snapshotRepository;
    private final DemandScoreCalculator demandCalculator;
    private final CompetitionScoreCalculator competitionCalculator;
    private final MarketScoreCalculator marketCalculator;
    private final StabilityScoreCalculator stabilityCalculator;

    public MetricAggregationService(
        RegionJpaRepository regionRepository,
        BusinessCategoryJpaRepository categoryRepository,
        MetricPeriodJpaRepository periodRepository,
        SourceSeoulSalesJpaRepository salesRepository,
        SourceSeoulStoresJpaRepository storesRepository,
        RegionPeriodFeatureJpaRepository featureRepository,
        CommercialMetricSnapshotJpaRepository snapshotRepository,
        DemandScoreCalculator demandCalculator,
        CompetitionScoreCalculator competitionCalculator,
        MarketScoreCalculator marketCalculator,
        StabilityScoreCalculator stabilityCalculator
    ) {
        this.regionRepository = regionRepository;
        this.categoryRepository = categoryRepository;
        this.periodRepository = periodRepository;
        this.salesRepository = salesRepository;
        this.storesRepository = storesRepository;
        this.featureRepository = featureRepository;
        this.snapshotRepository = snapshotRepository;
        this.demandCalculator = demandCalculator;
        this.competitionCalculator = competitionCalculator;
        this.marketCalculator = marketCalculator;
        this.stabilityCalculator = stabilityCalculator;
    }

    @Transactional
    public CommercialMetric aggregate(String regionCode, String categoryCode, String periodCode) {
        RegionEntity region = regionRepository.findByAdministrativeDongCodeAndActiveTrue(regionCode)
            .orElseThrow(() -> new ResourceNotFoundException("행정동을 찾을 수 없습니다: " + regionCode));
        BusinessCategoryEntity category = categoryRepository
            .findBySourceCategoryCodeAndActiveTrue(categoryCode)
            .orElseThrow(() -> new ResourceNotFoundException("업종을 찾을 수 없습니다: " + categoryCode));
        MetricPeriodEntity period = periodRepository.findByPeriodCode(periodCode)
            .orElseThrow(() -> new ResourceNotFoundException("지표 기간을 찾을 수 없습니다: " + periodCode));

        SourceSeoulSalesEntity sales = salesRepository
            .findByRegionIdAndBusinessCategoryIdAndMetricPeriodId(
                region.getId(),
                category.getId(),
                period.getId()
            )
            .orElseThrow(() -> new ResourceNotFoundException("해당 조건의 매출 원천 데이터가 없습니다."));
        SourceSeoulStoresEntity stores = storesRepository
            .findByRegionIdAndBusinessCategoryIdAndMetricPeriodId(
                region.getId(),
                category.getId(),
                period.getId()
            )
            .orElseThrow(() -> new ResourceNotFoundException("해당 조건의 점포 원천 데이터가 없습니다."));

        BigDecimal salesGrowth = previousSales(region, category, period)
            .map(previous -> SalesMetric.growthRate(sales.getSalesAmount(), previous.getSalesAmount()))
            .orElse(null);
        BigDecimal storeGrowth = previousStores(region, category, period)
            .map(previous -> SalesMetric.growthRate(stores.getStoreCount(), previous.getStoreCount()))
            .orElse(null);
        RegionPeriodFeatureEntity feature = featureRepository
            .findByRegionIdAndMetricPeriodId(region.getId(), period.getId())
            .orElse(null);

        DemandMetric demand = CommercialMetricMapper.toDemand(feature);
        SalesMetric salesMetric = new SalesMetric(
            sales.getSalesAmount(),
            sales.getSalesCount(),
            null,
            salesGrowth,
            null
        );
        StoreMetric storeMetric = new StoreMetric(
            stores.getStoreCount(),
            stores.getOpenStoreCount(),
            stores.getCloseStoreCount(),
            stores.getOpenRate(),
            stores.getCloseRate(),
            percentage(stores.getFranchiseStoreCount(), stores.getStoreCount()),
            storeGrowth
        );
        BigDecimal demandScore = demandCalculator.calculate(demand);
        BigDecimal competitionScore = competitionCalculator.calculate(storeMetric);
        BigDecimal marketScore = marketCalculator.calculate(demandScore, competitionScore, salesMetric);
        BigDecimal stabilityScore = stabilityCalculator.calculate(salesMetric, storeMetric);
        BigDecimal closureRisk = stabilityCalculator
            .calculateClosureRiskSignal(stabilityScore, storeMetric);

        CommercialMetricSnapshotEntity snapshot = snapshotRepository
            .findByRegionIdAndBusinessCategoryIdAndMetricPeriodId(
                region.getId(),
                category.getId(),
                period.getId()
            )
            .orElseGet(() -> new CommercialMetricSnapshotEntity(region, category, period));
        snapshot.applySources(sales, stores, salesGrowth, storeGrowth);
        snapshot.applyScores(
            competitionScore,
            demandScore,
            marketScore,
            stabilityScore,
            closureRisk
        );
        return CommercialMetricMapper.toDomain(snapshotRepository.save(snapshot), feature);
    }

    private java.util.Optional<SourceSeoulSalesEntity> previousSales(
        RegionEntity region,
        BusinessCategoryEntity category,
        MetricPeriodEntity period
    ) {
        return salesRepository
            .findFirstByRegionIdAndBusinessCategoryIdAndMetricPeriodStartDateBeforeOrderByMetricPeriodStartDateDesc(
                region.getId(),
                category.getId(),
                period.getStartDate()
            );
    }

    private java.util.Optional<SourceSeoulStoresEntity> previousStores(
        RegionEntity region,
        BusinessCategoryEntity category,
        MetricPeriodEntity period
    ) {
        return storesRepository
            .findFirstByRegionIdAndBusinessCategoryIdAndMetricPeriodStartDateBeforeOrderByMetricPeriodStartDateDesc(
                region.getId(),
                category.getId(),
                period.getStartDate()
            );
    }

    private static BigDecimal percentage(Integer part, Integer total) {
        if (part == null || total == null || total == 0) {
            return null;
        }
        return BigDecimal.valueOf(part * 100.0 / total);
    }
}
