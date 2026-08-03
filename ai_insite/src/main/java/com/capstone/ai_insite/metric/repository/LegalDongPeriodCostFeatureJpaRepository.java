package com.capstone.ai_insite.metric.repository;

import com.capstone.ai_insite.metric.entity.LegalDongPeriodCostFeatureEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LegalDongPeriodCostFeatureJpaRepository
    extends JpaRepository<LegalDongPeriodCostFeatureEntity, Long> {

    long deleteByMetricPeriodId(Long metricPeriodId);

    List<LegalDongPeriodCostFeatureEntity> findByMetricPeriodId(
        Long metricPeriodId
    );

    Optional<LegalDongPeriodCostFeatureEntity>
        findByLegalDongIdAndMetricPeriodIdAndPropertyType(
            Long legalDongId,
            Long metricPeriodId,
            String propertyType
        );
}
