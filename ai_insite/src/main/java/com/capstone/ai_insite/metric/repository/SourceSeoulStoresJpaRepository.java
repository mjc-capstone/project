package com.capstone.ai_insite.metric.repository;

import com.capstone.ai_insite.metric.entity.SourceSeoulStoresEntity;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceSeoulStoresJpaRepository extends JpaRepository<SourceSeoulStoresEntity, Long> {

    Optional<SourceSeoulStoresEntity>
        findByRegionIdAndBusinessCategoryIdAndMetricPeriodId(
            Long regionId,
            Long businessCategoryId,
            Long metricPeriodId
        );

    Optional<SourceSeoulStoresEntity>
        findFirstByRegionIdAndBusinessCategoryIdAndMetricPeriodStartDateBeforeOrderByMetricPeriodStartDateDesc(
            Long regionId,
            Long businessCategoryId,
            java.time.LocalDate startDate
        );

    List<SourceSeoulStoresEntity> findAllByMetricPeriodId(Long metricPeriodId);

    List<SourceSeoulStoresEntity> findByMetricPeriodIdAndRegionIdIn(
        Long metricPeriodId,
        java.util.Collection<Long> regionIds
    );
}
