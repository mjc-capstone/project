package com.capstone.ai_insite.metric.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record SalesMetric(
    Long salesAmount,
    Long salesCount,
    BigDecimal averageTicketAmount,
    BigDecimal growthRateQoq,
    BigDecimal growthRateYoy
) {
    public static BigDecimal growthRate(Number current, Number previous) {
        if (current == null || previous == null || previous.doubleValue() == 0) {
            return null;
        }
        return BigDecimal.valueOf(current.doubleValue() - previous.doubleValue())
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(previous.doubleValue()), 4, RoundingMode.HALF_UP);
    }
}
