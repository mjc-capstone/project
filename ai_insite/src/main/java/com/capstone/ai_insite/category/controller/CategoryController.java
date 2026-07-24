package com.capstone.ai_insite.category.controller;

import com.capstone.ai_insite.category.dto.BusinessCategoryResponse;
import com.capstone.ai_insite.category.service.CategoryQueryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryQueryService categoryQueryService;

    public CategoryController(CategoryQueryService categoryQueryService) {
        this.categoryQueryService = categoryQueryService;
    }

    @GetMapping
    public List<BusinessCategoryResponse> search(
        @RequestParam(required = false) String keyword
    ) {
        return categoryQueryService.search(keyword).stream()
            .map(BusinessCategoryResponse::from)
            .toList();
    }

    @GetMapping("/{categoryCode}")
    public BusinessCategoryResponse get(@PathVariable String categoryCode) {
        return BusinessCategoryResponse.from(categoryQueryService.getByCode(categoryCode));
    }
}
