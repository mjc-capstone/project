package com.capstone.ai_insite.category.repository;

import com.capstone.ai_insite.category.entity.CategoryCodeMappingEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryCodeMappingJpaRepository extends JpaRepository<CategoryCodeMappingEntity, Long> {

    List<CategoryCodeMappingEntity> findBySeoulServiceCategoryCode(String categoryCode);
}
