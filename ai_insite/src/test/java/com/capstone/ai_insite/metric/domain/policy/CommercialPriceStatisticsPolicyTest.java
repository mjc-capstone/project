package com.capstone.ai_insite.metric.domain.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class CommercialPriceStatisticsPolicyTest {

    private final CommercialPriceStatisticsPolicy policy =
        new CommercialPriceStatisticsPolicy();

    @Test
    void calculatesInterpolatedQuartilesAndMedian() {
        var result = policy.calculate(
            5,
            List.of(
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(300),
                BigDecimal.valueOf(400)
            )
        );

        assertEquals(new BigDecimal("250.00"), result.medianPricePerArea());
        assertEquals(new BigDecimal("175.00"), result.pricePerAreaP25());
        assertEquals(new BigDecimal("325.00"), result.pricePerAreaP75());
        assertEquals(5, result.transactionCount());
        assertEquals(4, result.pricedTransactionCount());
    }

    @Test
    void growthRequiresPreviousNonZeroMedian() {
        assertEquals(
            new BigDecimal("25.0000"),
            policy.growthRate(BigDecimal.valueOf(125), BigDecimal.valueOf(100))
        );
        assertNull(policy.growthRate(BigDecimal.TEN, BigDecimal.ZERO));
    }
}
