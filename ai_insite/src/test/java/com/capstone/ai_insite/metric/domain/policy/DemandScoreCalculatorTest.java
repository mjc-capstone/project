package com.capstone.ai_insite.metric.domain.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.capstone.ai_insite.metric.domain.DemandMetric;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class DemandScoreCalculatorTest {

    private final DemandScoreCalculator calculator = new DemandScoreCalculator();

    @Test
    void averagesOnlyAvailableNormalizedDemandScores() {
        DemandMetric demand = new DemandMetric(
            100_000L,
            20_000L,
            30_000L,
            BigDecimal.valueOf(80),
            BigDecimal.valueOf(60),
            null,
            null
        );

        assertEquals(new BigDecimal("70.0000"), calculator.calculate(demand));
    }

    @Test
    void returnsNeutralScoreWhenNormalizedScoresAreUnavailable() {
        assertEquals(
            new BigDecimal("50.0000"),
            calculator.calculate(DemandMetric.empty())
        );
    }
}
