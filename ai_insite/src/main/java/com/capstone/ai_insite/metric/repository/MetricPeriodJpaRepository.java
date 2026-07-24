package com.capstone.ai_insite.metric.repository;

import com.capstone.ai_insite.metric.entity.MetricPeriodEntity;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetricPeriodJpaRepository extends JpaRepository<MetricPeriodEntity, Long> {

    Optional<MetricPeriodEntity> findByPeriodCode(String periodCode);

    List<MetricPeriodEntity> findByPeriodCodeIn(java.util.Collection<String> periodCodes);

    Optional<MetricPeriodEntity> findFirstByStartDateBeforeOrderByStartDateDesc(
        java.time.LocalDate startDate
    );
}
