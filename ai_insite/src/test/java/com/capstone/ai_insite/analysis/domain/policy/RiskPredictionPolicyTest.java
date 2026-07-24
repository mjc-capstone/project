package com.capstone.ai_insite.analysis.domain.policy;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.capstone.ai_insite.analysis.domain.AnalysisPrediction;
import com.capstone.ai_insite.analysis.domain.UserBusinessCondition;
import com.capstone.ai_insite.metric.domain.CommercialMetric;
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
}
