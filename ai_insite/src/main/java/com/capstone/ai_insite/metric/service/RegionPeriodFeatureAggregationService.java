package com.capstone.ai_insite.metric.service;

import com.capstone.ai_insite.common.exception.ResourceNotFoundException;
import com.capstone.ai_insite.common.value.ScoreMath;
import com.capstone.ai_insite.metric.domain.RegionPeriodFeatureValues;
import com.capstone.ai_insite.metric.domain.policy.PercentileScorePolicy;
import com.capstone.ai_insite.metric.entity.AbstractSeoulRegionalSourceEntity;
import com.capstone.ai_insite.metric.entity.MetricPeriodEntity;
import com.capstone.ai_insite.metric.entity.RegionPeriodFeatureEntity;
import com.capstone.ai_insite.metric.entity.SourceSeoulApartmentsEntity;
import com.capstone.ai_insite.metric.entity.SourceSeoulFacilitiesEntity;
import com.capstone.ai_insite.metric.entity.SourceSeoulFloatingPopulationEntity;
import com.capstone.ai_insite.metric.entity.SourceSeoulResidentPopulationEntity;
import com.capstone.ai_insite.metric.entity.SourceSeoulWorkingPopulationEntity;
import com.capstone.ai_insite.metric.repository.MetricPeriodJpaRepository;
import com.capstone.ai_insite.metric.repository.RegionPeriodFeatureJpaRepository;
import com.capstone.ai_insite.metric.repository.SourceSeoulApartmentsJpaRepository;
import com.capstone.ai_insite.metric.repository.SourceSeoulFacilitiesJpaRepository;
import com.capstone.ai_insite.metric.repository.SourceSeoulFloatingPopulationJpaRepository;
import com.capstone.ai_insite.metric.repository.SourceSeoulResidentPopulationJpaRepository;
import com.capstone.ai_insite.metric.repository.SourceSeoulWorkingPopulationJpaRepository;
import com.capstone.ai_insite.region.entity.RegionEntity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class RegionPeriodFeatureAggregationService {

    private final MetricPeriodJpaRepository periodRepository;
    private final RegionPeriodFeatureJpaRepository featureRepository;
    private final SourceSeoulFloatingPopulationJpaRepository floatingRepository;
    private final SourceSeoulResidentPopulationJpaRepository residentRepository;
    private final SourceSeoulWorkingPopulationJpaRepository workingRepository;
    private final SourceSeoulFacilitiesJpaRepository facilitiesRepository;
    private final SourceSeoulApartmentsJpaRepository apartmentsRepository;
    private final PercentileScorePolicy percentilePolicy;
    private final ObjectMapper objectMapper;

    public RegionPeriodFeatureAggregationService(
        MetricPeriodJpaRepository periodRepository,
        RegionPeriodFeatureJpaRepository featureRepository,
        SourceSeoulFloatingPopulationJpaRepository floatingRepository,
        SourceSeoulResidentPopulationJpaRepository residentRepository,
        SourceSeoulWorkingPopulationJpaRepository workingRepository,
        SourceSeoulFacilitiesJpaRepository facilitiesRepository,
        SourceSeoulApartmentsJpaRepository apartmentsRepository,
        PercentileScorePolicy percentilePolicy,
        ObjectMapper objectMapper
    ) {
        this.periodRepository = periodRepository;
        this.featureRepository = featureRepository;
        this.floatingRepository = floatingRepository;
        this.residentRepository = residentRepository;
        this.workingRepository = workingRepository;
        this.facilitiesRepository = facilitiesRepository;
        this.apartmentsRepository = apartmentsRepository;
        this.percentilePolicy = percentilePolicy;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public int aggregatePeriod(String periodCode) {
        MetricPeriodEntity period = periodRepository.findByPeriodCode(periodCode)
            .orElseThrow(() -> new ResourceNotFoundException(
                "지표 기간을 찾을 수 없습니다: " + periodCode
            ));
        Map<Long, SourceSeoulFloatingPopulationEntity> floating = byRegion(
            floatingRepository.findAllByMetricPeriodId(period.getId())
        );
        Map<Long, SourceSeoulResidentPopulationEntity> resident = byRegion(
            residentRepository.findAllByMetricPeriodId(period.getId())
        );
        Map<Long, SourceSeoulWorkingPopulationEntity> working = byRegion(
            workingRepository.findAllByMetricPeriodId(period.getId())
        );
        Map<Long, SourceSeoulFacilitiesEntity> facilities = byRegion(
            facilitiesRepository.findAllByMetricPeriodId(period.getId())
        );
        Map<Long, SourceSeoulApartmentsEntity> apartments = byRegion(
            apartmentsRepository.findAllByMetricPeriodId(period.getId())
        );
        Set<Long> regionIds = union(
            floating.keySet(),
            resident.keySet(),
            working.keySet(),
            facilities.keySet(),
            apartments.keySet()
        );

        Map<Long, BigDecimal> residentScores = percentilePolicy.score(
            numericMap(regionIds, resident, SourceSeoulResidentPopulationEntity::getResidentPopulationTotal)
        );
        Map<Long, BigDecimal> householdScores = percentilePolicy.score(
            numericMap(regionIds, resident, SourceSeoulResidentPopulationEntity::getHouseholdCount)
        );
        Map<Long, BigDecimal> apartmentScores = percentilePolicy.score(
            numericMap(regionIds, apartments, SourceSeoulApartmentsEntity::getApartmentComplexCount)
        );
        Map<Long, BigDecimal> workingScores = percentilePolicy.score(
            numericMap(regionIds, working, SourceSeoulWorkingPopulationEntity::getWorkingPopulationTotal)
        );
        Map<Long, BigDecimal> attractionScores = percentilePolicy.score(
            numericMap(regionIds, facilities, SourceSeoulFacilitiesEntity::getFacilityTotalCount)
        );
        Map<Long, BigDecimal> trafficScores = percentilePolicy.score(
            trafficValues(regionIds, facilities)
        );

        Map<Long, RegionPeriodFeatureEntity> features = featureRepository
            .findAllByMetricPeriodId(period.getId())
            .stream()
            .collect(Collectors.toMap(
                feature -> feature.getRegion().getId(),
                Function.identity()
            ));
        for (Long regionId : regionIds) {
            SourceSeoulFloatingPopulationEntity floatingRow = floating.get(regionId);
            SourceSeoulResidentPopulationEntity residentRow = resident.get(regionId);
            SourceSeoulWorkingPopulationEntity workingRow = working.get(regionId);
            SourceSeoulFacilitiesEntity facilitiesRow = facilities.get(regionId);
            SourceSeoulApartmentsEntity apartmentsRow = apartments.get(regionId);
            RegionPeriodFeatureEntity feature = features.computeIfAbsent(
                regionId,
                ignored -> new RegionPeriodFeatureEntity(
                    region(
                        floatingRow,
                        residentRow,
                        workingRow,
                        facilitiesRow,
                        apartmentsRow
                    ),
                    period
                )
            );
            feature.apply(new RegionPeriodFeatureValues(
                value(floatingRow, SourceSeoulFloatingPopulationEntity::getFloatingPopulationTotal),
                value(floatingRow, SourceSeoulFloatingPopulationEntity::getFloatingPopulationByAgeJson),
                value(floatingRow, SourceSeoulFloatingPopulationEntity::getFloatingPopulationByTimeJson),
                value(residentRow, SourceSeoulResidentPopulationEntity::getResidentPopulationTotal),
                value(residentRow, SourceSeoulResidentPopulationEntity::getResidentPopulationByAgeJson),
                value(residentRow, SourceSeoulResidentPopulationEntity::getHouseholdCount),
                value(workingRow, SourceSeoulWorkingPopulationEntity::getWorkingPopulationTotal),
                value(workingRow, SourceSeoulWorkingPopulationEntity::getWorkingPopulationByAgeJson),
                value(facilitiesRow, SourceSeoulFacilitiesEntity::getFacilityTotalCount),
                value(facilitiesRow, SourceSeoulFacilitiesEntity::getFacilityDetailJson),
                value(apartmentsRow, SourceSeoulApartmentsEntity::getApartmentComplexCount),
                apartmentDetail(apartmentsRow),
                floatingRatio(floatingRow, "06_11", "11_14", "14_17"),
                floatingRatio(floatingRow, "21_24", "00_06"),
                floatingDayRatio(floatingRow, "SAT", "SUN"),
                averageAvailable(
                    residentScores.get(regionId),
                    householdScores.get(regionId),
                    apartmentScores.get(regionId)
                ),
                workingScores.get(regionId),
                attractionScores.get(regionId),
                trafficScores.get(regionId)
            ));
        }
        featureRepository.saveAll(features.values());
        return regionIds.size();
    }

    private BigDecimal floatingRatio(
        SourceSeoulFloatingPopulationEntity row,
        String... keys
    ) {
        return ratioFromJson(
            row == null ? null : row.getFloatingPopulationByTimeJson(),
            row == null ? null : row.getFloatingPopulationTotal(),
            keys
        );
    }

    private BigDecimal floatingDayRatio(
        SourceSeoulFloatingPopulationEntity row,
        String... keys
    ) {
        return ratioFromJson(
            row == null ? null : row.getFloatingPopulationByDayJson(),
            row == null ? null : row.getFloatingPopulationTotal(),
            keys
        );
    }

    private BigDecimal ratioFromJson(String json, Long total, String... keys) {
        if (json == null || total == null || total == 0) {
            return null;
        }
        try {
            JsonNode detail = objectMapper.readTree(json);
            long part = Arrays.stream(keys)
                .mapToLong(key -> detail.path(key).asLong())
                .sum();
            return BigDecimal.valueOf(part)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);
        } catch (Exception exception) {
            throw new IllegalStateException("생활인구 비율 계산에 실패했습니다.", exception);
        }
    }

    private String apartmentDetail(SourceSeoulApartmentsEntity row) {
        if (row == null) {
            return null;
        }
        try {
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("householdsByArea", objectMapper.readTree(
                row.getApartmentHouseholdsByAreaJson()
            ));
            detail.put("householdsByPrice", objectMapper.readTree(
                row.getApartmentHouseholdsByPriceJson()
            ));
            detail.put("averageArea", row.getAverageApartmentArea());
            detail.put("averageMarketPrice", row.getAverageApartmentMarketPrice());
            return objectMapper.writeValueAsString(detail);
        } catch (Exception exception) {
            throw new IllegalStateException("아파트 피처 JSON 생성에 실패했습니다.", exception);
        }
    }

    private static Map<Long, BigDecimal> trafficValues(
        Set<Long> regionIds,
        Map<Long, SourceSeoulFacilitiesEntity> facilities
    ) {
        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        for (Long regionId : regionIds) {
            SourceSeoulFacilitiesEntity row = facilities.get(regionId);
            if (row != null) {
                int total = intValue(row.getRailwayStationCount())
                    + intValue(row.getSubwayStationCount())
                    + intValue(row.getBusStopCount());
                result.put(regionId, BigDecimal.valueOf(total));
            }
        }
        return result;
    }

    private static int intValue(Integer value) {
        return value == null ? 0 : value;
    }

    private static <E, N extends Number> Map<Long, BigDecimal> numericMap(
        Set<Long> regionIds,
        Map<Long, E> entities,
        Function<E, N> getter
    ) {
        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        for (Long regionId : regionIds) {
            E entity = entities.get(regionId);
            N value = entity == null ? null : getter.apply(entity);
            result.put(regionId, value == null ? null : new BigDecimal(value.toString()));
        }
        return result;
    }

    private static BigDecimal averageAvailable(BigDecimal... values) {
        if (Arrays.stream(values).allMatch(value -> value == null)) {
            return null;
        }
        return ScoreMath.average(Arrays.asList(values));
    }

    @SafeVarargs
    private static Set<Long> union(Set<Long>... sets) {
        Set<Long> result = new LinkedHashSet<>();
        for (Set<Long> values : sets) {
            result.addAll(values);
        }
        return result;
    }

    private static RegionEntity region(
        AbstractSeoulRegionalSourceEntity<?>... sources
    ) {
        return Arrays.stream(sources)
            .filter(source -> source != null)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("행정동 원천 행을 찾을 수 없습니다."))
            .getRegion();
    }

    private static <E extends AbstractSeoulRegionalSourceEntity<?>> Map<Long, E> byRegion(
        List<E> entities
    ) {
        return entities.stream().collect(Collectors.toMap(
            entity -> entity.getRegion().getId(),
            Function.identity(),
            (first, ignored) -> first,
            LinkedHashMap::new
        ));
    }

    private static <T, R> R value(T source, Function<T, R> getter) {
        return source == null ? null : getter.apply(source);
    }
}
