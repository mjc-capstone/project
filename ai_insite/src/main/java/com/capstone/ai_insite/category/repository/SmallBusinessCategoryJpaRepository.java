package com.capstone.ai_insite.category.repository;

import com.capstone.ai_insite.category.entity.SmallBusinessCategoryEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmallBusinessCategoryJpaRepository
    extends JpaRepository<SmallBusinessCategoryEntity, Long> {

    Optional<SmallBusinessCategoryEntity> findBySmallCategoryCode(String code);

    List<SmallBusinessCategoryEntity> findBySmallCategoryCodeIn(
        Collection<String> codes
    );

    List<SmallBusinessCategoryEntity> findByActiveTrue();
}
