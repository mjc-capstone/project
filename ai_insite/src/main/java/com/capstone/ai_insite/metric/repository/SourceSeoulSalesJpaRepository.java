package com.capstone.ai_insite.metric.repository;

import com.capstone.ai_insite.metric.entity.SourceSeoulSalesEntity;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceSeoulSalesJpaRepository extends JpaRepository<SourceSeoulSalesEntity, Long> {

    Optional<SourceSeoulSalesEntity>
        findByRegionIdAndBusinessCategoryIdAndMetricPeriodId(
            Long regionId,
            Long businessCategoryId,
            Long metricPeriodId
        );

    Optional<SourceSeoulSalesEntity>
        findFirstByRegionIdAndBusinessCategoryIdAndMetricPeriodStartDateBeforeOrderByMetricPeriodStartDateDesc(
            Long regionId,
            Long businessCategoryId,
            java.time.LocalDate startDate
        );

    List<SourceSeoulSalesEntity> findAllByMetricPeriodId(Long metricPeriodId);

    List<SourceSeoulSalesEntity> findByMetricPeriodIdAndRegionIdIn(
        Long metricPeriodId,
        java.util.Collection<Long> regionIds
    );
}
