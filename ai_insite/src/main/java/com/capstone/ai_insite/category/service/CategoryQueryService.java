package com.capstone.ai_insite.category.service;

import com.capstone.ai_insite.category.domain.BusinessCategory;
import com.capstone.ai_insite.category.entity.BusinessCategoryEntity;
import com.capstone.ai_insite.category.repository.BusinessCategoryJpaRepository;
import com.capstone.ai_insite.common.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CategoryQueryService {

    private final BusinessCategoryJpaRepository categoryRepository;

    public CategoryQueryService(BusinessCategoryJpaRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<BusinessCategory> search(String keyword) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return categoryRepository.searchActive(normalizedKeyword).stream()
            .map(CategoryQueryService::toDomain)
            .toList();
    }

    public BusinessCategory getByCode(String categoryCode) {
        return categoryRepository.findBySourceCategoryCodeAndActiveTrue(categoryCode)
            .map(CategoryQueryService::toDomain)
            .orElseThrow(() -> new ResourceNotFoundException("업종을 찾을 수 없습니다: " + categoryCode));
    }

    private static BusinessCategory toDomain(BusinessCategoryEntity entity) {
        return new BusinessCategory(
            entity.getId(),
            entity.getSourceSystem(),
            entity.getSourceCategoryCode(),
            entity.getSourceCategoryName(),
            entity.getLargeCategoryName(),
            entity.getMediumCategoryName(),
            entity.getSmallCategoryName(),
            entity.getNormalizedCategoryCode(),
            entity.getNormalizedCategoryName()
        );
    }
}
