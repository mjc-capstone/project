package com.capstone.ai_insite.metric.repository;

import com.capstone.ai_insite.metric.entity.RegionCostFeatureEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionCostFeatureJpaRepository
    extends JpaRepository<RegionCostFeatureEntity, Long> {

    long deleteByMetricPeriodIdAndSourceSystem(
        Long metricPeriodId,
        String sourceSystem
    );

    Optional<RegionCostFeatureEntity>
        findByRegionIdAndMetricPeriodIdAndSourceSystemAndPropertyType(
            Long regionId,
            Long metricPeriodId,
            String sourceSystem,
            String propertyType
        );

    Optional<RegionCostFeatureEntity>
        findFirstByMetricPeriodIdAndSourceSystemAndSourceRegionNameAndRegionLevelAndPropertyType(
            Long metricPeriodId,
            String sourceSystem,
            String sourceRegionName,
            String regionLevel,
            String propertyType
        );
}
