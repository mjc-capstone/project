package com.capstone.ai_insite.analysis.domain.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.capstone.ai_insite.analysis.domain.UserBusinessCondition;
import com.capstone.ai_insite.metric.domain.CostFeatureContext;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CostFitPolicyTest {

    private final CostFitPolicy policy = new CostFitPolicy();

    @Test
    void estimatesMonthlyRentAndPenalizesInsufficientRentLimit() {
        CostFeatureContext cost = new CostFeatureContext(
            BigDecimal.valueOf(50), null, null, null, null, null,
            BigDecimal.valueOf(20), null, null, null, null
        );
        UserBusinessCondition affordable = condition(3_000_000L);
        UserBusinessCondition constrained = condition(1_000_000L);

        assertEquals(
            new BigDecimal("2000000"),
            policy.estimatedMonthlyRent(affordable, cost)
        );
        assertTrue(
            policy.score(affordable, cost)
                .compareTo(policy.score(constrained, cost)) > 0
        );
    }

    private static UserBusinessCondition condition(long maxRent) {
        return new UserBusinessCondition(
            100_000_000L,
            maxRent,
            20_000_000L,
            BigDecimal.valueOf(40),
            "OWNER",
            false,
            null
        );
    }
}
