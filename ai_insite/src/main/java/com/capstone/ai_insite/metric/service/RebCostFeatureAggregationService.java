package com.capstone.ai_insite.metric.service;

import com.capstone.ai_insite.common.value.ScoreMath;
import com.capstone.ai_insite.metric.domain.policy.PercentileScorePolicy;
import com.capstone.ai_insite.metric.entity.MetricPeriodEntity;
import com.capstone.ai_insite.metric.entity.RegionCostFeatureEntity;
import com.capstone.ai_insite.metric.entity.SourceRebCommercialRentStatEntity;
import com.capstone.ai_insite.metric.repository.RegionCostFeatureJpaRepository;
import com.capstone.ai_insite.metric.repository.SourceRebCommercialRentStatJpaRepository;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RebCostFeatureAggregationService {

    public static final String SOURCE_SYSTEM = "REB";
    public static final String CALCULATION_VERSION = "cost-v1";

    private final SourceRebCommercialRentStatJpaRepository sourceRepository;
    private final RegionCostFeatureJpaRepository featureRepository;
    private final PercentileScorePolicy percentileScorePolicy;

    public RebCostFeatureAggregationService(
        SourceRebCommercialRentStatJpaRepository sourceRepository,
        RegionCostFeatureJpaRepository featureRepository,
        PercentileScorePolicy percentileScorePolicy
    ) {
        this.sourceRepository = sourceRepository;
        this.featureRepository = featureRepository;
        this.percentileScorePolicy = percentileScorePolicy;
    }

    @Transactional
    public int rebuild(MetricPeriodEntity period) {
        List<RebAggregate> aggregates = aggregate(
            sourceRepository.findByMetricPeriodId(period.getId())
        );
        Map<RebKey, BigDecimal> rentScores = scores(
            aggregates,
            RebAggregate::rentAmount
        );
        Map<RebKey, BigDecimal> vacancyScores = scores(
            aggregates,
            RebAggregate::vacancyRate
        );
        featureRepository.deleteByMetricPeriodIdAndSourceSystem(
            period.getId(),
            SOURCE_SYSTEM
        );
        featureRepository.flush();
        List<RegionCostFeatureEntity> features = aggregates.stream()
            .map(value -> new RegionCostFeatureEntity(
                null,
                null,
                period,
                SOURCE_SYSTEM,
                "REB:" + value.key().sourceRegionCode(),
                value.key().regionLevel(),
                value.key().sourceRegionCode(),
                value.key().sourceRegionName(),
                value.key().propertyType(),
                value.rentAmount(),
                value.rentIndex(),
                value.vacancyRate(),
                value.investmentReturnRate(),
                null,
                null,
                null,
                rentScores.get(value.key()),
                vacancyScores.get(value.key()),
                fixedCost(
                    rentScores.get(value.key()),
                    vacancyScores.get(value.key())
                ),
                null,
                value.observationCount(),
                value.rentUnit(),
                null,
                CALCULATION_VERSION
            ))
            .toList();
        return featureRepository.saveAll(features).size();
    }

    private List<RebAggregate> aggregate(
        List<SourceRebCommercialRentStatEntity> sources
    ) {
        Map<RebKey, RebAggregate> values = new LinkedHashMap<>();
        for (SourceRebCommercialRentStatEntity source : sources) {
            RebKey key = new RebKey(
                source.getSourceRegionCode(),
                source.getSourceRegionName(),
                source.getSourceRegionFullName(),
                source.getRegionLevel(),
                source.getPropertyType()
            );
            values.computeIfAbsent(key, RebAggregate::new).put(source);
        }
        return List.copyOf(values.values());
    }

    private Map<RebKey, BigDecimal> scores(
        List<RebAggregate> values,
        ValueExtractor extractor
    ) {
        Map<ScoreBucket, List<RebAggregate>> buckets = values.stream()
            .collect(Collectors.groupingBy(
                value -> new ScoreBucket(
                    value.key().propertyType(),
                    value.key().regionLevel()
                ),
                LinkedHashMap::new,
                Collectors.toList()
            ));
        Map<RebKey, BigDecimal> result = new LinkedHashMap<>();
        for (List<RebAggregate> bucket : buckets.values()) {
            Map<RebKey, BigDecimal> input = new LinkedHashMap<>();
            bucket.forEach(value -> input.put(value.key(), extractor.get(value)));
            result.putAll(percentileScorePolicy.score(input));
        }
        return result;
    }

    private static BigDecimal fixedCost(
        BigDecimal rentScore,
        BigDecimal vacancyScore
    ) {
        if (rentScore == null) {
            return vacancyScore;
        }
        if (vacancyScore == null) {
            return rentScore;
        }
        return ScoreMath.weighted(rentScore, 0.65, vacancyScore, 0.35);
    }

    private record RebKey(
        String sourceRegionCode,
        String sourceRegionName,
        String sourceRegionFullName,
        String regionLevel,
        String propertyType
    ) {
    }

    private record ScoreBucket(String propertyType, String regionLevel) {
    }

    @FunctionalInterface
    private interface ValueExtractor {
        BigDecimal get(RebAggregate value);
    }

    private static final class RebAggregate {

        private final RebKey key;
        private final Map<String, BigDecimal> values = new LinkedHashMap<>();
        private String rentUnit;

        private RebAggregate(RebKey key) {
            this.key = key;
        }

        private void put(SourceRebCommercialRentStatEntity source) {
            values.put(source.getMetricType(), source.getMetricValue());
            if ("RENT_AMOUNT".equals(source.getMetricType())) {
                rentUnit = source.getUnitName();
            }
        }

        private RebKey key() {
            return key;
        }

        private BigDecimal rentAmount() {
            return values.get("RENT_AMOUNT");
        }

        private BigDecimal rentIndex() {
            return values.get("RENT_INDEX");
        }

        private BigDecimal vacancyRate() {
            return values.get("VACANCY_RATE");
        }

        private BigDecimal investmentReturnRate() {
            return values.get("INVESTMENT_RETURN_RATE");
        }

        private int observationCount() {
            return values.size();
        }

        private String rentUnit() {
            return rentUnit;
        }
    }
}
