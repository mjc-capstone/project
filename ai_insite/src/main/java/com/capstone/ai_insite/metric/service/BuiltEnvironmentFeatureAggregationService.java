package com.capstone.ai_insite.metric.service;

import com.capstone.ai_insite.common.value.ScoreMath;
import com.capstone.ai_insite.metric.domain.BuildingObservation;
import com.capstone.ai_insite.metric.domain.BuiltEnvironmentStatistics;
import com.capstone.ai_insite.metric.domain.policy.BuiltEnvironmentStatisticsPolicy;
import com.capstone.ai_insite.metric.domain.policy.PercentileScorePolicy;
import com.capstone.ai_insite.metric.entity.BuildingRegionMappingEntity;
import com.capstone.ai_insite.metric.entity.MetricPeriodEntity;
import com.capstone.ai_insite.metric.entity.RegionBuiltEnvironmentFeatureEntity;
import com.capstone.ai_insite.metric.entity.SourceMolitBuildingRegisterEntity;
import com.capstone.ai_insite.metric.repository.BuildingRegionMappingJpaRepository;
import com.capstone.ai_insite.metric.repository.RegionBuiltEnvironmentFeatureJpaRepository;
import com.capstone.ai_insite.metric.repository.SourceMolitBuildingRegisterJpaRepository;
import com.capstone.ai_insite.region.entity.LegalDongEntity;
import com.capstone.ai_insite.region.entity.RegionEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BuiltEnvironmentFeatureAggregationService {

    public static final String CALCULATION_VERSION = "building-v1";

    private final SourceMolitBuildingRegisterJpaRepository sourceRepository;
    private final BuildingRegionMappingJpaRepository mappingRepository;
    private final RegionBuiltEnvironmentFeatureJpaRepository featureRepository;
    private final BuiltEnvironmentStatisticsPolicy statisticsPolicy;
    private final PercentileScorePolicy percentileScorePolicy;

    public BuiltEnvironmentFeatureAggregationService(
        SourceMolitBuildingRegisterJpaRepository sourceRepository,
        BuildingRegionMappingJpaRepository mappingRepository,
        RegionBuiltEnvironmentFeatureJpaRepository featureRepository,
        BuiltEnvironmentStatisticsPolicy statisticsPolicy,
        PercentileScorePolicy percentileScorePolicy
    ) {
        this.sourceRepository = sourceRepository;
        this.mappingRepository = mappingRepository;
        this.featureRepository = featureRepository;
        this.statisticsPolicy = statisticsPolicy;
        this.percentileScorePolicy = percentileScorePolicy;
    }

    @Transactional
    public int rebuild(MetricPeriodEntity period, LocalDate snapshotDate) {
        List<SourceMolitBuildingRegisterEntity> sources =
            sourceRepository.findBySnapshotDate(snapshotDate);
        Map<Long, List<SourceMolitBuildingRegisterEntity>> legalGroups =
            sources.stream().collect(Collectors.groupingBy(
                source -> source.getLegalDong().getId(),
                LinkedHashMap::new,
                Collectors.toList()
            ));
        Map<Long, BuildingRegionMappingEntity> mappingsBySource =
            mappingRepository.findBySourceBuildingSnapshotDate(snapshotDate)
                .stream()
                .collect(Collectors.toMap(
                    mapping -> mapping.getSourceBuilding().getId(),
                    mapping -> mapping,
                    (left, right) -> left,
                    LinkedHashMap::new
                ));
        Map<Long, List<SourceMolitBuildingRegisterEntity>> regionGroups =
            sources.stream()
                .filter(source -> {
                    BuildingRegionMappingEntity mapping =
                        mappingsBySource.get(source.getId());
                    return mapping != null && mapping.isConfirmed();
                })
                .collect(Collectors.groupingBy(
                    source -> mappingsBySource.get(source.getId()).getRegion().getId(),
                    LinkedHashMap::new,
                    Collectors.toList()
                ));

        List<RegionDraft> regionDrafts = regionGroups.values().stream()
            .map(group -> new RegionDraft(
                mappingsBySource.get(group.getFirst().getId()).getRegion(),
                statistics(group, snapshotDate)
            ))
            .toList();
        Map<Long, BigDecimal> physicalScores = physicalScores(regionDrafts);

        featureRepository.deleteByMetricPeriodIdAndSnapshotDate(
            period.getId(),
            snapshotDate
        );
        featureRepository.flush();
        List<RegionBuiltEnvironmentFeatureEntity> legalFeatures =
            legalGroups.values().stream()
                .map(group -> legalFeature(period, snapshotDate, group))
                .toList();
        List<RegionBuiltEnvironmentFeatureEntity> regionFeatures =
            regionDrafts.stream()
                .map(draft -> regionFeature(
                    period,
                    snapshotDate,
                    draft,
                    physicalScores.get(draft.region().getId())
                ))
                .toList();
        featureRepository.saveAll(legalFeatures);
        featureRepository.saveAll(regionFeatures);
        return legalFeatures.size() + regionFeatures.size();
    }

    private RegionBuiltEnvironmentFeatureEntity legalFeature(
        MetricPeriodEntity period,
        LocalDate snapshotDate,
        List<SourceMolitBuildingRegisterEntity> group
    ) {
        LegalDongEntity legalDong = group.getFirst().getLegalDong();
        return new RegionBuiltEnvironmentFeatureEntity(
            null,
            legalDong,
            period,
            snapshotDate,
            "LEGAL:" + legalDong.getLegalDongCode(),
            "LEGAL_DONG",
            statistics(group, snapshotDate),
            null,
            CALCULATION_VERSION
        );
    }

    private RegionBuiltEnvironmentFeatureEntity regionFeature(
        MetricPeriodEntity period,
        LocalDate snapshotDate,
        RegionDraft draft,
        BigDecimal physicalScore
    ) {
        RegionEntity region = draft.region();
        return new RegionBuiltEnvironmentFeatureEntity(
            region,
            null,
            period,
            snapshotDate,
            "ADMIN:" + region.getAdministrativeDongCode(),
            "ADMINISTRATIVE_DONG",
            draft.statistics(),
            physicalScore,
            CALCULATION_VERSION
        );
    }

    private BuiltEnvironmentStatistics statistics(
        List<SourceMolitBuildingRegisterEntity> group,
        LocalDate snapshotDate
    ) {
        return statisticsPolicy.calculate(
            group.stream().map(source -> new BuildingObservation(
                source.getMainUseCode(),
                source.getApprovalDate(),
                source.getGrossFloorAreaSquareMeter(),
                source.getParkingCount()
            )).toList(),
            snapshotDate
        );
    }

    private Map<Long, BigDecimal> physicalScores(List<RegionDraft> drafts) {
        Map<Long, BigDecimal> counts = new LinkedHashMap<>();
        Map<Long, BigDecimal> areas = new LinkedHashMap<>();
        Map<Long, BigDecimal> parking = new LinkedHashMap<>();
        Map<Long, BigDecimal> ages = new LinkedHashMap<>();
        drafts.forEach(draft -> {
            Long id = draft.region().getId();
            BuiltEnvironmentStatistics value = draft.statistics();
            counts.put(id, BigDecimal.valueOf(value.commercialBuildingCount()));
            areas.put(id, value.commercialFloorAreaProxy());
            parking.put(id, value.parkingSpacesPerCommercialBuilding());
            ages.put(id, value.averageBuildingAge());
        });
        Map<Long, BigDecimal> countScores = percentileScorePolicy.score(counts);
        Map<Long, BigDecimal> areaScores = percentileScorePolicy.score(areas);
        Map<Long, BigDecimal> parkingScores = percentileScorePolicy.score(parking);
        Map<Long, BigDecimal> ageScores = percentileScorePolicy.score(ages);
        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        drafts.forEach(draft -> {
            Long id = draft.region().getId();
            BigDecimal ageFit = ageScores.containsKey(id)
                ? BigDecimal.valueOf(100).subtract(ageScores.get(id))
                : ScoreMath.NEUTRAL;
            result.put(id, ScoreMath.clamp(
                score(countScores.get(id)).multiply(BigDecimal.valueOf(0.30))
                    .add(score(areaScores.get(id)).multiply(BigDecimal.valueOf(0.30)))
                    .add(score(parkingScores.get(id)).multiply(BigDecimal.valueOf(0.20)))
                    .add(ageFit.multiply(BigDecimal.valueOf(0.20)))
            ));
        });
        return result;
    }

    private static BigDecimal score(BigDecimal value) {
        return value == null ? ScoreMath.NEUTRAL : value;
    }

    private record RegionDraft(
        RegionEntity region,
        BuiltEnvironmentStatistics statistics
    ) {
    }
}
