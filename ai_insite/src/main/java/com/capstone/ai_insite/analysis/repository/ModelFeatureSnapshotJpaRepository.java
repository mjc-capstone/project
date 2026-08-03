package com.capstone.ai_insite.analysis.repository;

import com.capstone.ai_insite.analysis.entity.ModelFeatureSnapshotEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelFeatureSnapshotJpaRepository
    extends JpaRepository<ModelFeatureSnapshotEntity, Long> {

    Optional<ModelFeatureSnapshotEntity>
        findByRegionIdAndBusinessCategoryIdAndMetricPeriodIdAndFeatureVersion(
            Long regionId,
            Long businessCategoryId,
            Long metricPeriodId,
            String featureVersion
        );

    List<ModelFeatureSnapshotEntity>
        findByFeatureVersionAndMetricPeriodStartDateBetweenOrderByMetricPeriodStartDateAsc(
            String featureVersion,
            LocalDate from,
            LocalDate to
        );
}
