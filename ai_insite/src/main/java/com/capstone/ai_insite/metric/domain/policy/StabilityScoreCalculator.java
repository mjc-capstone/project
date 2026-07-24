package com.capstone.ai_insite.metric.domain.policy;

import com.capstone.ai_insite.common.value.ScoreMath;
import com.capstone.ai_insite.metric.domain.SalesMetric;
import com.capstone.ai_insite.metric.domain.StoreMetric;
import java.math.BigDecimal;

public class StabilityScoreCalculator {

    public BigDecimal calculate(SalesMetric sales, StoreMetric stores) {
        BigDecimal survivalSignal = ScoreMath.clamp(
            BigDecimal.valueOf(100).subtract(valueOrZero(stores.closeRate()))
        );
        BigDecimal momentum = ScoreMath.clamp(
            ScoreMath.NEUTRAL.add(valueOrZero(sales.growthRateQoq()))
        );
        return ScoreMath.weighted(survivalSignal, 0.65, momentum, 0.35);
    }

    public BigDecimal calculateClosureRiskSignal(BigDecimal stability, StoreMetric stores) {
        BigDecimal inverseStability = BigDecimal.valueOf(100).subtract(stability);
        BigDecimal closeRate = ScoreMath.clamp(valueOrZero(stores.closeRate()));
        return ScoreMath.weighted(inverseStability, 0.7, closeRate, 0.3);
    }

    private static BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
