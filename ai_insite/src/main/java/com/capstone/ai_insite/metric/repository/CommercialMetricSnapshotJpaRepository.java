package com.capstone.ai_insite.metric.repository;

import com.capstone.ai_insite.metric.entity.CommercialMetricSnapshotEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommercialMetricSnapshotJpaRepository
    extends JpaRepository<CommercialMetricSnapshotEntity, Long> {

    Optional<CommercialMetricSnapshotEntity>
        findByRegionIdAndBusinessCategoryIdAndMetricPeriodId(
            Long regionId,
            Long businessCategoryId,
            Long metricPeriodId
        );

    List<CommercialMetricSnapshotEntity> findAllByMetricPeriodId(Long metricPeriodId);

    Optional<CommercialMetricSnapshotEntity>
        findByRegionAdministrativeDongCodeAndBusinessCategorySourceCategoryCodeAndMetricPeriodPeriodCode(
            String regionCode,
            String categoryCode,
            String periodCode
        );

    List<CommercialMetricSnapshotEntity>
        findByRegionAdministrativeDongCodeAndBusinessCategorySourceCategoryCodeAndMetricPeriodStartDateBetweenOrderByMetricPeriodStartDate(
            String regionCode,
            String categoryCode,
            LocalDate from,
            LocalDate to
        );
}
