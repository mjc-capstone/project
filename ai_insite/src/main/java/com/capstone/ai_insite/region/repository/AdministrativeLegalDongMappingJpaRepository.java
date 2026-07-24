package com.capstone.ai_insite.region.repository;

import com.capstone.ai_insite.region.entity.AdministrativeLegalDongMappingEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministrativeLegalDongMappingJpaRepository
    extends JpaRepository<AdministrativeLegalDongMappingEntity, Long> {

    List<AdministrativeLegalDongMappingEntity>
        findByRegionAdministrativeDongCodeOrderByMappingConfidenceDesc(String administrativeDongCode);
}
