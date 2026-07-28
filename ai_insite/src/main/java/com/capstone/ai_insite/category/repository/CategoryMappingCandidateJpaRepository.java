package com.capstone.ai_insite.category.repository;

import com.capstone.ai_insite.category.domain.MappingStatus;
import com.capstone.ai_insite.category.entity.CategoryMappingCandidateEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryMappingCandidateJpaRepository
    extends JpaRepository<CategoryMappingCandidateEntity, Long> {

    Optional<CategoryMappingCandidateEntity>
        findBySmallBusinessCategoryId(Long smallBusinessCategoryId);

    List<CategoryMappingCandidateEntity>
        findByMappingStatusOrderByEvidenceCountDesc(
            MappingStatus status,
            Pageable pageable
        );

    List<CategoryMappingCandidateEntity> findAllByOrderByEvidenceCountDesc(
        Pageable pageable
    );
}
