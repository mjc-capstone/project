package com.capstone.ai_insite.metric.service;

import com.capstone.ai_insite.common.exception.ResourceNotFoundException;
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
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MetricBatchAggregationService {

    private final MetricPeriodJpaRepository periodRepository;
    private final SourceSeoulSalesJpaRepository salesRepository;
    private final SourceSeoulStoresJpaRepository storesRepository;
    private final RegionPeriodFeatureJpaRepository featureRepository;
    private final CommercialMetricSnapshotJpaRepository snapshotRepository;
    private final DemandScoreCalculator demandCalculator;
    private final CompetitionScoreCalculator competitionCalculator;
    private final MarketScoreCalculator marketCalculator;
    private final StabilityScoreCalculator stabilityCalculator;

    public MetricBatchAggregationService(
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
    public int aggregatePeriod(String periodCode) {
        MetricPeriodEntity period = periodRepository.findByPeriodCode(periodCode)
            .orElseThrow(() -> new ResourceNotFoundException(
                "지표 기간을 찾을 수 없습니다: " + periodCode
            ));
        List<SourceSeoulSalesEntity> salesRows = salesRepository
            .findAllByMetricPeriodId(period.getId());
        Map<MetricKey, SourceSeoulStoresEntity> stores = storesRepository
            .findAllByMetricPeriodId(period.getId())
            .stream()
            .collect(Collectors.toMap(
                MetricBatchAggregationService::key,
                Function.identity(),
                (first, ignored) -> first,
                LinkedHashMap::new
            ));
        Map<Long, RegionPeriodFeatureEntity> features = featureRepository
            .findAllByMetricPeriodId(period.getId())
            .stream()
            .collect(Collectors.toMap(
                feature -> feature.getRegion().getId(),
                Function.identity()
            ));
        Map<MetricKey, CommercialMetricSnapshotEntity> snapshots = snapshotRepository
            .findAllByMetricPeriodId(period.getId())
            .stream()
            .collect(Collectors.toMap(
                MetricBatchAggregationService::key,
                Function.identity(),
                (first, ignored) -> first,
                LinkedHashMap::new
            ));
        PreviousSources previous = previousSources(period);

        int aggregated = 0;
        for (SourceSeoulSalesEntity sales : salesRows) {
            MetricKey key = key(sales);
            SourceSeoulStoresEntity store = stores.get(key);
            if (store == null) {
                continue;
            }
            BigDecimal salesGrowth = SalesMetric.growthRate(
                sales.getSalesAmount(),
                value(previous.sales().get(key), SourceSeoulSalesEntity::getSalesAmount)
            );
            BigDecimal storeGrowth = SalesMetric.growthRate(
                store.getStoreCount(),
                value(previous.stores().get(key), SourceSeoulStoresEntity::getStoreCount)
            );
            RegionPeriodFeatureEntity feature = features.get(sales.getRegion().getId());
            DemandMetric demand = CommercialMetricMapper.toDemand(feature);
            SalesMetric salesMetric = new SalesMetric(
                sales.getSalesAmount(),
                sales.getSalesCount(),
                null,
                salesGrowth,
                null
            );
            StoreMetric storeMetric = new StoreMetric(
                store.getStoreCount(),
                store.getOpenStoreCount(),
                store.getCloseStoreCount(),
                store.getOpenRate(),
                store.getCloseRate(),
                percentage(store.getFranchiseStoreCount(), store.getStoreCount()),
                storeGrowth
            );
            BigDecimal demandScore = demandCalculator.calculate(demand);
            BigDecimal competitionScore = competitionCalculator.calculate(storeMetric);
            BigDecimal marketScore = marketCalculator.calculate(
                demandScore,
                competitionScore,
                salesMetric
            );
            BigDecimal stabilityScore = stabilityCalculator.calculate(salesMetric, storeMetric);
            BigDecimal closureRisk = stabilityCalculator.calculateClosureRiskSignal(
                stabilityScore,
                storeMetric
            );
            CommercialMetricSnapshotEntity snapshot = snapshots.computeIfAbsent(
                key,
                ignored -> new CommercialMetricSnapshotEntity(
                    sales.getRegion(),
                    sales.getBusinessCategory(),
                    period
                )
            );
            snapshot.applySources(sales, store, salesGrowth, storeGrowth);
            snapshot.applyScores(
                competitionScore,
                demandScore,
                marketScore,
                stabilityScore,
                closureRisk
            );
            aggregated++;
        }
        snapshotRepository.saveAll(snapshots.values());
        return aggregated;
    }

    private PreviousSources previousSources(MetricPeriodEntity currentPeriod) {
        return periodRepository
            .findFirstByStartDateBeforeOrderByStartDateDesc(currentPeriod.getStartDate())
            .map(previous -> new PreviousSources(
                salesRepository.findAllByMetricPeriodId(previous.getId()).stream()
                    .collect(Collectors.toMap(
                        MetricBatchAggregationService::key,
                        Function.identity(),
                        (first, ignored) -> first
                    )),
                storesRepository.findAllByMetricPeriodId(previous.getId()).stream()
                    .collect(Collectors.toMap(
                        MetricBatchAggregationService::key,
                        Function.identity(),
                        (first, ignored) -> first
                    ))
            ))
            .orElseGet(() -> new PreviousSources(Map.of(), Map.of()));
    }

    private static MetricKey key(SourceSeoulSalesEntity entity) {
        return new MetricKey(entity.getRegion().getId(), entity.getBusinessCategory().getId());
    }

    private static MetricKey key(SourceSeoulStoresEntity entity) {
        return new MetricKey(entity.getRegion().getId(), entity.getBusinessCategory().getId());
    }

    private static MetricKey key(CommercialMetricSnapshotEntity entity) {
        return new MetricKey(entity.getRegion().getId(), entity.getBusinessCategory().getId());
    }

    private static <T, R extends Number> R value(T source, Function<T, R> getter) {
        return source == null ? null : getter.apply(source);
    }

    private static BigDecimal percentage(Integer part, Integer total) {
        if (part == null || total == null || total == 0) {
            return null;
        }
        return BigDecimal.valueOf(part * 100.0 / total);
    }

    private record MetricKey(Long regionId, Long categoryId) {
    }

    private record PreviousSources(
        Map<MetricKey, SourceSeoulSalesEntity> sales,
        Map<MetricKey, SourceSeoulStoresEntity> stores
    ) {
    }
}
