package com.capstone.ai_insite.metric.service;

import com.capstone.ai_insite.metric.domain.CommercialPriceStatistics;
import com.capstone.ai_insite.metric.domain.policy.CommercialPriceStatisticsPolicy;
import com.capstone.ai_insite.metric.domain.policy.PercentileScorePolicy;
import com.capstone.ai_insite.metric.entity.LegalDongPeriodCostFeatureEntity;
import com.capstone.ai_insite.metric.entity.MetricPeriodEntity;
import com.capstone.ai_insite.metric.entity.RegionCostFeatureEntity;
import com.capstone.ai_insite.metric.entity.SourceMolitCommercialTransactionEntity;
import com.capstone.ai_insite.metric.repository.LegalDongPeriodCostFeatureJpaRepository;
import com.capstone.ai_insite.metric.repository.MetricPeriodJpaRepository;
import com.capstone.ai_insite.metric.repository.RegionCostFeatureJpaRepository;
import com.capstone.ai_insite.metric.repository.SourceMolitCommercialTransactionJpaRepository;
import com.capstone.ai_insite.region.entity.AdministrativeLegalDongMappingEntity;
import com.capstone.ai_insite.region.entity.LegalDongEntity;
import com.capstone.ai_insite.region.entity.RegionEntity;
import com.capstone.ai_insite.region.repository.AdministrativeLegalDongMappingJpaRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommercialTransactionCostFeatureAggregationService {

    public static final String SOURCE_SYSTEM = "MOLIT";
    public static final String PROPERTY_TYPE = "ALL_COMMERCIAL";
    public static final String CALCULATION_VERSION = "cost-v1";

    private final SourceMolitCommercialTransactionJpaRepository sourceRepository;
    private final LegalDongPeriodCostFeatureJpaRepository legalFeatureRepository;
    private final RegionCostFeatureJpaRepository regionFeatureRepository;
    private final MetricPeriodJpaRepository periodRepository;
    private final AdministrativeLegalDongMappingJpaRepository mappingRepository;
    private final CommercialPriceStatisticsPolicy statisticsPolicy;
    private final PercentileScorePolicy percentileScorePolicy;

    public CommercialTransactionCostFeatureAggregationService(
        SourceMolitCommercialTransactionJpaRepository sourceRepository,
        LegalDongPeriodCostFeatureJpaRepository legalFeatureRepository,
        RegionCostFeatureJpaRepository regionFeatureRepository,
        MetricPeriodJpaRepository periodRepository,
        AdministrativeLegalDongMappingJpaRepository mappingRepository,
        CommercialPriceStatisticsPolicy statisticsPolicy,
        PercentileScorePolicy percentileScorePolicy
    ) {
        this.sourceRepository = sourceRepository;
        this.legalFeatureRepository = legalFeatureRepository;
        this.regionFeatureRepository = regionFeatureRepository;
        this.periodRepository = periodRepository;
        this.mappingRepository = mappingRepository;
        this.statisticsPolicy = statisticsPolicy;
        this.percentileScorePolicy = percentileScorePolicy;
    }

    @Transactional
    public int rebuild(MetricPeriodEntity period) {
        List<SourceMolitCommercialTransactionEntity> sources =
            sourceRepository.findByDealDateBetweenAndCancelledFalse(
                period.getStartDate(),
                period.getEndDate()
            );
        Optional<MetricPeriodEntity> previousPeriod = previousPeriod(period);
        Map<Long, List<SourceMolitCommercialTransactionEntity>> legalGroups =
            sources.stream()
                .filter(source -> source.getLegalDong() != null)
                .collect(Collectors.groupingBy(
                    source -> source.getLegalDong().getId(),
                    LinkedHashMap::new,
                    Collectors.toList()
                ));
        List<LegalDongPeriodCostFeatureEntity> legalFeatures = legalGroups.values()
            .stream()
            .map(group -> legalFeature(period, previousPeriod, group))
            .toList();

        Map<Long, RegionEntity> uniquelyMappedRegions = uniqueRegionMappings(
            legalGroups.keySet()
        );
        Map<Long, List<SourceMolitCommercialTransactionEntity>> regionGroups =
            sources.stream()
                .filter(source -> source.getLegalDong() != null)
                .filter(source -> uniquelyMappedRegions.containsKey(
                    source.getLegalDong().getId()
                ))
                .collect(Collectors.groupingBy(
                    source -> uniquelyMappedRegions
                        .get(source.getLegalDong().getId())
                        .getId(),
                    LinkedHashMap::new,
                    Collectors.toList()
                ));
        List<RegionDraft> regionDrafts = regionGroups.entrySet().stream()
            .map(entry -> regionDraft(
                uniquelyMappedRegions.values().stream()
                    .filter(region -> region.getId().equals(entry.getKey()))
                    .findFirst()
                    .orElseThrow(),
                entry.getValue(),
                previousPeriod
            ))
            .toList();
        Map<Long, BigDecimal> medianPrices = new LinkedHashMap<>();
        regionDrafts.forEach(draft -> medianPrices.put(
            draft.region().getId(),
            draft.statistics().medianPricePerArea()
        ));
        Map<Long, BigDecimal> locationScores =
            percentileScorePolicy.score(medianPrices);

        legalFeatureRepository.deleteByMetricPeriodId(period.getId());
        regionFeatureRepository.deleteByMetricPeriodIdAndSourceSystem(
            period.getId(),
            SOURCE_SYSTEM
        );
        legalFeatureRepository.saveAll(legalFeatures);
        List<RegionCostFeatureEntity> regionFeatures = regionDrafts.stream()
            .map(draft -> regionFeature(
                period,
                draft,
                locationScores.get(draft.region().getId())
            ))
            .toList();
        regionFeatureRepository.saveAll(regionFeatures);
        return legalFeatures.size() + regionFeatures.size();
    }

    private LegalDongPeriodCostFeatureEntity legalFeature(
        MetricPeriodEntity period,
        Optional<MetricPeriodEntity> previousPeriod,
        List<SourceMolitCommercialTransactionEntity> group
    ) {
        LegalDongEntity legalDong = group.getFirst().getLegalDong();
        CommercialPriceStatistics statistics = statistics(group);
        BigDecimal previousMedian = previousPeriod
            .flatMap(previous -> legalFeatureRepository
                .findByLegalDongIdAndMetricPeriodIdAndPropertyType(
                    legalDong.getId(),
                    previous.getId(),
                    PROPERTY_TYPE
                ))
            .map(LegalDongPeriodCostFeatureEntity::getMedianCommercialPricePerArea)
            .orElse(null);
        return new LegalDongPeriodCostFeatureEntity(
            legalDong,
            period,
            PROPERTY_TYPE,
            statistics.transactionCount(),
            statistics.medianPricePerArea(),
            statistics.averagePricePerArea(),
            statistics.pricePerAreaP25(),
            statistics.pricePerAreaP75(),
            statisticsPolicy.growthRate(
                statistics.medianPricePerArea(),
                previousMedian
            ),
            group.size(),
            CALCULATION_VERSION
        );
    }

    private RegionDraft regionDraft(
        RegionEntity region,
        List<SourceMolitCommercialTransactionEntity> group,
        Optional<MetricPeriodEntity> previousPeriod
    ) {
        CommercialPriceStatistics statistics = statistics(group);
        BigDecimal previousMedian = previousPeriod
            .flatMap(previous -> regionFeatureRepository
                .findByRegionIdAndMetricPeriodIdAndSourceSystemAndPropertyType(
                    region.getId(),
                    previous.getId(),
                    SOURCE_SYSTEM,
                    PROPERTY_TYPE
                ))
            .map(RegionCostFeatureEntity::getMedianCommercialPricePerArea)
            .orElse(null);
        return new RegionDraft(
            region,
            statistics,
            statisticsPolicy.growthRate(
                statistics.medianPricePerArea(),
                previousMedian
            ),
            group.size()
        );
    }

    private RegionCostFeatureEntity regionFeature(
        MetricPeriodEntity period,
        RegionDraft draft,
        BigDecimal locationCostScore
    ) {
        RegionEntity region = draft.region();
        return new RegionCostFeatureEntity(
            region,
            null,
            period,
            SOURCE_SYSTEM,
            "ADMIN:" + region.getAdministrativeDongCode(),
            "ADMINISTRATIVE_DONG",
            region.getAdministrativeDongCode(),
            region.getAdministrativeDongName(),
            PROPERTY_TYPE,
            null,
            null,
            null,
            null,
            draft.statistics().transactionCount(),
            draft.statistics().medianPricePerArea(),
            draft.priceGrowthRate(),
            null,
            null,
            null,
            locationCostScore,
            draft.sourceObservationCount(),
            null,
            "원/㎡",
            CALCULATION_VERSION
        );
    }

    private CommercialPriceStatistics statistics(
        List<SourceMolitCommercialTransactionEntity> group
    ) {
        return statisticsPolicy.calculate(
            group.size(),
            group.stream()
                .map(SourceMolitCommercialTransactionEntity::getPricePerBuildingArea)
                .toList()
        );
    }

    private Map<Long, RegionEntity> uniqueRegionMappings(
        Collection<Long> legalDongIds
    ) {
        Map<Long, List<AdministrativeLegalDongMappingEntity>> byLegalDong =
            mappingRepository.findByLegalDongIdIn(legalDongIds).stream()
                .filter(AdministrativeLegalDongMappingEntity::isUsable)
                .collect(Collectors.groupingBy(
                    mapping -> mapping.getLegalDong().getId(),
                    LinkedHashMap::new,
                    Collectors.toList()
                ));
        Map<Long, RegionEntity> result = new LinkedHashMap<>();
        byLegalDong.forEach((legalDongId, mappings) -> {
            Map<Long, RegionEntity> distinctRegions = mappings.stream()
                .collect(Collectors.toMap(
                    mapping -> mapping.getRegion().getId(),
                    AdministrativeLegalDongMappingEntity::getRegion,
                    (left, right) -> left,
                    LinkedHashMap::new
                ));
            if (distinctRegions.size() == 1) {
                result.put(legalDongId, distinctRegions.values().iterator().next());
            }
        });
        return result;
    }

    private Optional<MetricPeriodEntity> previousPeriod(
        MetricPeriodEntity period
    ) {
        int previousYear = period.getQuarter() == 1
            ? period.getYear() - 1
            : period.getYear();
        int previousQuarter = period.getQuarter() == 1
            ? 4
            : period.getQuarter() - 1;
        return periodRepository.findByPeriodCode(
            previousYear + String.valueOf(previousQuarter)
        );
    }

    private record RegionDraft(
        RegionEntity region,
        CommercialPriceStatistics statistics,
        BigDecimal priceGrowthRate,
        int sourceObservationCount
    ) {
    }
}
