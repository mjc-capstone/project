package com.capstone.ai_insite.metric.service;

import com.capstone.ai_insite.metric.domain.policy.CompetitionDensityPolicy;
import com.capstone.ai_insite.metric.domain.policy.CompetitionRadiusPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CompetitionPolicyConfiguration {

    @Bean
    CompetitionDensityPolicy competitionDensityPolicy() {
        return new CompetitionDensityPolicy();
    }

    @Bean
    CompetitionRadiusPolicy competitionRadiusPolicy() {
        return new CompetitionRadiusPolicy();
    }
}
