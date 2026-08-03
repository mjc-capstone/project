package com.capstone.ai_insite.metric.service;

import com.capstone.ai_insite.metric.domain.policy.BuildingUsePolicy;
import com.capstone.ai_insite.metric.domain.policy.BuiltEnvironmentStatisticsPolicy;
import com.capstone.ai_insite.metric.domain.policy.CompetitionScoreCalculator;
import com.capstone.ai_insite.metric.domain.policy.CommercialPriceStatisticsPolicy;
import com.capstone.ai_insite.metric.domain.policy.DemandScoreCalculator;
import com.capstone.ai_insite.metric.domain.policy.MarketScoreCalculator;
import com.capstone.ai_insite.metric.domain.policy.PercentileScorePolicy;
import com.capstone.ai_insite.metric.domain.policy.StabilityScoreCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricPolicyConfiguration {

    @Bean
    DemandScoreCalculator demandScoreCalculator() {
        return new DemandScoreCalculator();
    }

    @Bean
    CompetitionScoreCalculator competitionScoreCalculator() {
        return new CompetitionScoreCalculator();
    }

    @Bean
    MarketScoreCalculator marketScoreCalculator() {
        return new MarketScoreCalculator();
    }

    @Bean
    StabilityScoreCalculator stabilityScoreCalculator() {
        return new StabilityScoreCalculator();
    }

    @Bean
    PercentileScorePolicy percentileScorePolicy() {
        return new PercentileScorePolicy();
    }

    @Bean
    CommercialPriceStatisticsPolicy commercialPriceStatisticsPolicy() {
        return new CommercialPriceStatisticsPolicy();
    }

    @Bean
    BuildingUsePolicy buildingUsePolicy() {
        return new BuildingUsePolicy();
    }

    @Bean
    BuiltEnvironmentStatisticsPolicy builtEnvironmentStatisticsPolicy(
        BuildingUsePolicy buildingUsePolicy
    ) {
        return new BuiltEnvironmentStatisticsPolicy(buildingUsePolicy);
    }
}
