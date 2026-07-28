package com.capstone.ai_insite.metric.repository;

import com.capstone.ai_insite.metric.entity.CommercialCompetitionFeatureEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommercialCompetitionFeatureJpaRepository
    extends JpaRepository<CommercialCompetitionFeatureEntity, Long> {

    List<CommercialCompetitionFeatureEntity> findBySnapshotDate(LocalDate snapshotDate);

    void deleteBySnapshotDate(LocalDate snapshotDate);

    Optional<CommercialCompetitionFeatureEntity>
        findFirstByRegionIdAndBusinessCategoryIdAndMetricPeriodIdOrderBySnapshotDateDesc(
            Long regionId,
            Long businessCategoryId,
            Long metricPeriodId
        );
}
