package com.capstone.ai_insite.category.repository;

import com.capstone.ai_insite.category.entity.BusinessCategoryEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BusinessCategoryJpaRepository extends JpaRepository<BusinessCategoryEntity, Long> {

    Optional<BusinessCategoryEntity> findBySourceCategoryCodeAndActiveTrue(String sourceCategoryCode);

    Optional<BusinessCategoryEntity> findBySourceSystemAndSourceCategoryCode(
        String sourceSystem,
        String sourceCategoryCode
    );

    List<BusinessCategoryEntity> findBySourceSystemAndSourceCategoryCodeIn(
        String sourceSystem,
        java.util.Collection<String> sourceCategoryCodes
    );

    List<BusinessCategoryEntity> findByActiveTrue();

    @Query("""
        select c from BusinessCategoryEntity c
        where c.active = true
          and (:keyword is null
            or lower(c.sourceCategoryName) like lower(concat('%', :keyword, '%'))
            or lower(c.normalizedCategoryName) like lower(concat('%', :keyword, '%')))
        order by c.sourceCategoryName
        """)
    List<BusinessCategoryEntity> searchActive(@Param("keyword") String keyword);
}
