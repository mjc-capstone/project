package com.capstone.ai_insite.metric.domain.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class CompetitionDensityPolicyTest {

    private final CompetitionDensityPolicy policy = new CompetitionDensityPolicy();

    @Test
    void equalCategoryDistributionHasMaximumDiversity() {
        assertEquals(
            new BigDecimal("100.000000"),
            policy.categoryDiversityIndex(List.of(10L, 10L, 10L))
        );
    }

    @Test
    void concentratedDistributionHasLowerDiversity() {
        BigDecimal diversity = policy.categoryDiversityIndex(
            List.of(98L, 1L, 1L)
        );

        assertTrue(diversity.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(diversity.compareTo(new BigDecimal("50")) < 0);
    }
}
