package com.capstone.ai_insite.region.service;

import com.capstone.ai_insite.region.domain.policy.StoreRegionMappingPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RegionPolicyConfiguration {

    @Bean
    StoreRegionMappingPolicy storeRegionMappingPolicy() {
        return new StoreRegionMappingPolicy();
    }
}
