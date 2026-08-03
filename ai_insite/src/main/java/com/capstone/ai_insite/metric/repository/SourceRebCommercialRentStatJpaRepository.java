package com.capstone.ai_insite.metric.repository;

import com.capstone.ai_insite.metric.entity.SourceRebCommercialRentStatEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceRebCommercialRentStatJpaRepository
    extends JpaRepository<SourceRebCommercialRentStatEntity, Long> {

    long deleteByMetricPeriodId(Long metricPeriodId);

    List<SourceRebCommercialRentStatEntity> findByMetricPeriodId(
        Long metricPeriodId
    );
}
