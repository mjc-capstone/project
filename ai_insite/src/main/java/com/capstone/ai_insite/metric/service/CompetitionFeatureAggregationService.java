package com.capstone.ai_insite.metric.service;

import com.capstone.ai_insite.category.repository.BusinessCategoryJpaRepository;
import com.capstone.ai_insite.dataimport.repository.SourceSmallBusinessStoreJpaRepository;
import com.capstone.ai_insite.metric.domain.policy.CompetitionDensityPolicy;
import com.capstone.ai_insite.metric.entity.CommercialCompetitionFeatureEntity;
import com.capstone.ai_insite.metric.entity.MetricPeriodEntity;
import com.capstone.ai_insite.metric.repository.CommercialCompetitionFeatureJpaRepository;
import com.capstone.ai_insite.metric.repository.MetricPeriodJpaRepository;
import com.capstone.ai_insite.region.repository.RegionJpaRepository;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompetitionFeatureAggregationService {

    private final SourceSmallBusinessStoreJpaRepository sourceRepository;
    private final CommercialCompetitionFeatureJpaRepository featureRepository;
    private final MetricPeriodJpaRepository periodRepository;
    private final RegionJpaRepository regionRepository;
    private final BusinessCategoryJpaRepository categoryRepository;
    private final CompetitionDensityPolicy policy;

    public CompetitionFeatureAggregationService(
        SourceSmallBusinessStoreJpaRepository sourceRepository,
        CommercialCompetitionFeatureJpaRepository featureRepository,
        MetricPeriodJpaRepository periodRepository,
        RegionJpaRepository regionRepository,
        BusinessCategoryJpaRepository categoryRepository,
        CompetitionDensityPolicy policy
    ) {
        this.sourceRepository = sourceRepository;
        this.featureRepository = featureRepository;
        this.periodRepository = periodRepository;
        this.regionRepository = regionRepository;
        this.categoryRepository = categoryRepository;
        this.policy = policy;
    }

    @Transactional
    public int aggregate(LocalDate snapshotDate) {
        var categoryAggregates =
            sourceRepository.aggregateByRegionAndCategory(snapshotDate);
        if (categoryAggregates.isEmpty()) {
            return 0;
        }
        var regionAggregates = sourceRepository.aggregateByRegion(snapshotDate);
        Map<Long, Integer> regionTotals = regionAggregates.stream()
            .collect(Collectors.toMap(
                SourceSmallBusinessStoreJpaRepository.StoreRegionAggregate::getRegionId,
                aggregate -> Math.toIntExact(aggregate.getStoreCount())
            ));
        Map<Long, List<Long>> categoryCountsByRegion = categoryAggregates.stream()
            .collect(Collectors.groupingBy(
                SourceSmallBusinessStoreJpaRepository.StoreCategoryAggregate::getRegionId,
                Collectors.mapping(
                    SourceSmallBusinessStoreJpaRepository.StoreCategoryAggregate::getStoreCount,
                    Collectors.toList()
                )
            ));
        var regions = regionRepository.findAllById(
            categoryAggregates.stream()
                .map(SourceSmallBusinessStoreJpaRepository.StoreCategoryAggregate::getRegionId)
                .collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(
            region -> region.getId(),
            Function.identity()
        ));
        var categories = categoryRepository.findAllById(
            categoryAggregates.stream()
                .map(SourceSmallBusinessStoreJpaRepository.StoreCategoryAggregate::getCategoryId)
                .collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(
            category -> category.getId(),
            Function.identity()
        ));
        MetricPeriodEntity period = period(snapshotDate);
        Map<FeatureKey, CommercialCompetitionFeatureEntity> existing =
            featureRepository.findBySnapshotDate(snapshotDate).stream()
                .collect(Collectors.toMap(
                    entity -> new FeatureKey(
                        entity.getRegion().getId(),
                        entity.getBusinessCategory().getId()
                    ),
                    Function.identity()
                ));

        Map<FeatureKey, CommercialCompetitionFeatureEntity> changed = new HashMap<>();
        for (var aggregate : categoryAggregates) {
            FeatureKey key = new FeatureKey(
                aggregate.getRegionId(),
                aggregate.getCategoryId()
            );
            var region = regions.get(key.regionId());
            var category = categories.get(key.categoryId());
            if (region == null || category == null) {
                continue;
            }
            CommercialCompetitionFeatureEntity feature = existing.getOrDefault(
                key,
                new CommercialCompetitionFeatureEntity(
                    region,
                    category,
                    period,
                    snapshotDate
                )
            );
            int regionTotal = regionTotals.getOrDefault(key.regionId(), 0);
            feature.update(
                regionTotal,
                Math.toIntExact(aggregate.getStoreCount()),
                policy.categoryDiversityIndex(
                    categoryCountsByRegion.getOrDefault(key.regionId(), List.of())
                ),
                regionTotal
            );
            changed.put(key, feature);
        }
        featureRepository.saveAll(changed.values());
        return changed.size();
    }

    private MetricPeriodEntity period(LocalDate snapshotDate) {
        int quarter = (snapshotDate.getMonthValue() - 1) / 3 + 1;
        String code = snapshotDate.getYear() + "Q" + quarter;
        return periodRepository.findByPeriodCode(code)
            .orElseGet(() -> periodRepository.save(
                MetricPeriodEntity.createQuarter(code, snapshotDate.getYear(), quarter)
            ));
    }

    private record FeatureKey(Long regionId, Long categoryId) {
    }
}
