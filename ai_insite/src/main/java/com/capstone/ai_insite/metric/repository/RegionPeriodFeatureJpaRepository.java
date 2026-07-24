package com.capstone.ai_insite.metric.repository;

import com.capstone.ai_insite.metric.entity.RegionPeriodFeatureEntity;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionPeriodFeatureJpaRepository
    extends JpaRepository<RegionPeriodFeatureEntity, Long> {

    Optional<RegionPeriodFeatureEntity> findByRegionIdAndMetricPeriodId(
        Long regionId,
        Long metricPeriodId
    );

    List<RegionPeriodFeatureEntity> findAllByMetricPeriodId(Long metricPeriodId);
}
