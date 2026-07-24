package com.capstone.ai_insite.analysis.service;

import com.capstone.ai_insite.analysis.domain.policy.RecommendationPolicy;
import com.capstone.ai_insite.analysis.domain.policy.RiskPredictionPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnalysisPolicyConfiguration {

    @Bean
    RiskPredictionPolicy riskPredictionPolicy() {
        return new RiskPredictionPolicy();
    }

    @Bean
    RecommendationPolicy recommendationPolicy() {
        return new RecommendationPolicy();
    }
}
