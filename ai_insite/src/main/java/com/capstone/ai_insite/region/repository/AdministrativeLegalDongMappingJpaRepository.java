package com.capstone.ai_insite.region.repository;

import com.capstone.ai_insite.region.entity.AdministrativeLegalDongMappingEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import com.capstone.ai_insite.region.domain.RegionMappingStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministrativeLegalDongMappingJpaRepository
    extends JpaRepository<AdministrativeLegalDongMappingEntity, Long> {

    List<AdministrativeLegalDongMappingEntity>
        findByRegionAdministrativeDongCodeOrderByMappingConfidenceDesc(String administrativeDongCode);

    List<AdministrativeLegalDongMappingEntity> findByLegalDongIdIn(
        Collection<Long> legalDongIds
    );

    Optional<AdministrativeLegalDongMappingEntity>
        findFirstByRegionIdAndLegalDongIdOrderByIdDesc(
            Long regionId,
            Long legalDongId
        );

    List<AdministrativeLegalDongMappingEntity>
        findByMappingStatusOrderByEvidenceCountDesc(
            RegionMappingStatus status,
            Pageable pageable
        );

    List<AdministrativeLegalDongMappingEntity>
        findAllByOrderByEvidenceCountDesc(Pageable pageable);
}
