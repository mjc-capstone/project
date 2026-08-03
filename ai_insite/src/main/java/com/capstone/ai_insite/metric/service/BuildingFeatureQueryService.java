package com.capstone.ai_insite.metric.service;

import com.capstone.ai_insite.metric.domain.BuildingFeatureContext;
import com.capstone.ai_insite.metric.repository.MetricPeriodJpaRepository;
import com.capstone.ai_insite.metric.repository.RegionBuiltEnvironmentFeatureJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BuildingFeatureQueryService {

    private final RegionBuiltEnvironmentFeatureJpaRepository repository;
    private final MetricPeriodJpaRepository periodRepository;

    public BuildingFeatureQueryService(
        RegionBuiltEnvironmentFeatureJpaRepository repository,
        MetricPeriodJpaRepository periodRepository
    ) {
        this.repository = repository;
        this.periodRepository = periodRepository;
    }

    @Transactional(readOnly = true)
    public BuildingFeatureContext find(Long regionId, String periodCode) {
        return periodRepository.findByPeriodCode(periodCode)
            .map(period -> find(regionId, period.getId()))
            .orElseGet(BuildingFeatureContext::empty);
    }

    @Transactional(readOnly = true)
    public BuildingFeatureContext find(Long regionId, Long metricPeriodId) {
        return repository
            .findFirstByRegionIdAndMetricPeriodIdOrderBySnapshotDateDesc(
                regionId,
                metricPeriodId
            )
            .map(entity -> new BuildingFeatureContext(
                entity.getTotalBuildingCount(),
                entity.getCommercialBuildingCount(),
                entity.getAverageBuildingAge(),
                entity.getAgedBuildingRatio(),
                entity.getAverageGrossFloorArea(),
                entity.getTotalParkingCount(),
                entity.getParkingSpacesPerCommercialBuilding(),
                entity.getCommercialFloorAreaProxy(),
                entity.getCommercialFloorAreaRatio(),
                entity.getPhysicalEnvironmentScore()
            ))
            .orElseGet(BuildingFeatureContext::empty);
    }
}
