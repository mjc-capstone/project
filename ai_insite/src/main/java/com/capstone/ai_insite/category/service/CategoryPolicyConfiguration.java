package com.capstone.ai_insite.category.service;

import com.capstone.ai_insite.category.domain.policy.CategoryMappingPolicy;
import com.capstone.ai_insite.category.domain.policy.CategoryMappingCandidatePolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CategoryPolicyConfiguration {

    @Bean
    CategoryMappingPolicy categoryMappingPolicy() {
        return new CategoryMappingPolicy();
    }

    @Bean
    CategoryMappingCandidatePolicy categoryMappingCandidatePolicy() {
        return new CategoryMappingCandidatePolicy();
    }
}
