package com.capstone.ai_insite.metric.repository;

import com.capstone.ai_insite.metric.entity.BuildingRegionMappingEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingRegionMappingJpaRepository
    extends JpaRepository<BuildingRegionMappingEntity, Long> {

    long deleteBySourceBuildingLegalDongIdAndSourceBuildingSnapshotDate(
        Long legalDongId,
        LocalDate snapshotDate
    );

    List<BuildingRegionMappingEntity>
        findBySourceBuildingSnapshotDate(LocalDate snapshotDate);
}
