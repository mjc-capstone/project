package com.capstone.ai_insite.region.repository;

import com.capstone.ai_insite.region.entity.RegionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegionJpaRepository extends JpaRepository<RegionEntity, Long> {

    Optional<RegionEntity> findByAdministrativeDongCodeAndActiveTrue(String administrativeDongCode);

    Optional<RegionEntity> findByAdministrativeDongCode(String administrativeDongCode);

    List<RegionEntity> findByAdministrativeDongCodeIn(
        java.util.Collection<String> administrativeDongCodes
    );

    @Query("""
        select r from RegionEntity r
        where r.active = true
          and (:keyword is null
            or lower(r.administrativeDongName) like lower(concat('%', :keyword, '%'))
            or lower(r.sigunguName) like lower(concat('%', :keyword, '%')))
        order by r.sigunguName, r.administrativeDongName
        """)
    List<RegionEntity> searchActive(@Param("keyword") String keyword);
}
