package com.capstone.ai_insite.analysis.domain.policy;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.capstone.ai_insite.analysis.domain.AnalysisPrediction;
import com.capstone.ai_insite.analysis.domain.UserBusinessCondition;
import com.capstone.ai_insite.metric.domain.CommercialMetric;
import com.capstone.ai_insite.metric.domain.BuildingFeatureContext;
import com.capstone.ai_insite.metric.domain.CostFeatureContext;
import com.capstone.ai_insite.metric.domain.DemandMetric;
import com.capstone.ai_insite.metric.domain.MetricScores;
import com.capstone.ai_insite.metric.domain.SalesMetric;
import com.capstone.ai_insite.metric.domain.StoreMetric;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class RiskPredictionPolicyTest {

    private final RiskPredictionPolicy policy = new RiskPredictionPolicy();

    @Test
    void matchingTargetSalesImprovesSuccessScore() {
        CommercialMetric metric = metric();
        AnalysisPrediction matching = policy.predict(metric, condition(10_000_000L));
        AnalysisPrediction excessive = policy.predict(metric, condition(50_000_000L));

        assertTrue(matching.successScore().compareTo(excessive.successScore()) > 0);
        assertTrue(matching.positiveFactors().stream().anyMatch(it -> it.contains("목표 매출")));
        assertTrue(excessive.riskFactors().stream().anyMatch(it -> it.contains("목표 매출")));
    }

    @Test
    void buildingEnvironmentChangesSuccessAndClosureRisk() {
        BuildingFeatureContext favorable = building(
            new BigDecimal("85"),
            new BigDecimal("10")
        );
        BuildingFeatureContext poor = building(
            new BigDecimal("20"),
            new BigDecimal("80")
        );

        AnalysisPrediction favorableResult = policy.predict(
            metric(),
            condition(10_000_000L),
            CostFeatureContext.empty(),
            favorable
        );
        AnalysisPrediction poorResult = policy.predict(
            metric(),
            condition(10_000_000L),
            CostFeatureContext.empty(),
            poor
        );

        assertTrue(
            favorableResult.successScore().compareTo(poorResult.successScore()) > 0
        );
        assertTrue(
            favorableResult.closureRiskScore()
                .compareTo(poorResult.closureRiskScore()) < 0
        );
    }

    private static CommercialMetric metric() {
        return new CommercialMetric(
            1L,
            "1168064000",
            "역삼1동",
            "CS100001",
            "한식음식점",
            "2025Q1",
            new SalesMetric(300_000_000L, 20_000L, null, BigDecimal.TEN, null),
            new StoreMetric(
                10,
                1,
                1,
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(20),
                BigDecimal.ZERO
            ),
            new DemandMetric(
                100_000L,
                20_000L,
                30_000L,
                BigDecimal.valueOf(75),
                BigDecimal.valueOf(70),
                BigDecimal.valueOf(65),
                BigDecimal.valueOf(80)
            ),
            new MetricScores(
                BigDecimal.valueOf(72.5),
                BigDecimal.valueOf(40),
                BigDecimal.valueOf(70),
                BigDecimal.valueOf(75),
                BigDecimal.valueOf(20)
            )
        );
    }

    private static UserBusinessCondition condition(Long targetSales) {
        return new UserBusinessCondition(
            100_000_000L,
            3_000_000L,
            targetSales,
            BigDecimal.valueOf(40),
            "OWNER",
            false,
            null
        );
    }

    private static BuildingFeatureContext building(
        BigDecimal physicalScore,
        BigDecimal agedRatio
    ) {
        return new BuildingFeatureContext(
            100,
            40,
            BigDecimal.valueOf(20),
            agedRatio,
            BigDecimal.valueOf(200),
            100,
            BigDecimal.valueOf(2.5),
            BigDecimal.valueOf(8_000),
            BigDecimal.valueOf(60),
            physicalScore
        );
    }
}
