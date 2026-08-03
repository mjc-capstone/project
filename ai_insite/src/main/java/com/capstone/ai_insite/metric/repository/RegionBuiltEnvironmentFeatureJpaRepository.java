package com.capstone.ai_insite.metric.repository;

import com.capstone.ai_insite.metric.entity.RegionBuiltEnvironmentFeatureEntity;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionBuiltEnvironmentFeatureJpaRepository
    extends JpaRepository<RegionBuiltEnvironmentFeatureEntity, Long> {

    long deleteByMetricPeriodIdAndSnapshotDate(
        Long metricPeriodId,
        LocalDate snapshotDate
    );

    Optional<RegionBuiltEnvironmentFeatureEntity>
        findFirstByRegionIdAndMetricPeriodIdOrderBySnapshotDateDesc(
            Long regionId,
            Long metricPeriodId
        );
}
