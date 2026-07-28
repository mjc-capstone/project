package com.capstone.ai_insite.region.repository;

import com.capstone.ai_insite.region.entity.LegalDongEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LegalDongJpaRepository extends JpaRepository<LegalDongEntity, Long> {

    Optional<LegalDongEntity> findByLegalDongCodeAndActiveTrue(String legalDongCode);

    List<LegalDongEntity> findByLegalDongCodeIn(Collection<String> legalDongCodes);

    List<LegalDongEntity> findByActiveTrue();
}
