package com.capstone.ai_insite.metric.repository;

import com.capstone.ai_insite.metric.entity.SourceMolitBuildingRegisterEntity;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceMolitBuildingRegisterJpaRepository
    extends JpaRepository<SourceMolitBuildingRegisterEntity, Long> {

    long deleteByLegalDongIdAndSnapshotDate(Long legalDongId, LocalDate snapshotDate);

    List<SourceMolitBuildingRegisterEntity> findBySnapshotDate(LocalDate snapshotDate);

    List<SourceMolitBuildingRegisterEntity>
        findByLegalDongIdInAndSnapshotDate(
            Collection<Long> legalDongIds,
            LocalDate snapshotDate
        );
}
