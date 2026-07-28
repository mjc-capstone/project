package com.capstone.ai_insite.category.repository;

import com.capstone.ai_insite.category.entity.CategoryCodeMappingEntity;
import com.capstone.ai_insite.category.domain.MappingReviewType;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface CategoryCodeMappingJpaRepository extends JpaRepository<CategoryCodeMappingEntity, Long> {

    List<CategoryCodeMappingEntity> findBySeoulServiceCategoryCode(String categoryCode);

    List<CategoryCodeMappingEntity> findBySmallBusinessCategoryCodeIn(
        Collection<String> categoryCodes
    );

    List<CategoryCodeMappingEntity> findByKsicCodeIn(Collection<String> ksicCodes);

    @Transactional
    void deleteByReviewType(MappingReviewType reviewType);

    @Transactional
    void deleteBySmallBusinessCategoryCodeAndReviewType(
        String smallBusinessCategoryCode,
        MappingReviewType reviewType
    );

    @Transactional
    void deleteBySmallBusinessCategoryCode(String smallBusinessCategoryCode);
}
