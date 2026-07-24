package com.capstone.ai_insite.metric.domain.policy;

import com.capstone.ai_insite.common.value.ScoreMath;
import com.capstone.ai_insite.metric.domain.SalesMetric;
import java.math.BigDecimal;

public class MarketScoreCalculator {

    public BigDecimal calculate(
        BigDecimal demandScore,
        BigDecimal competitionScore,
        SalesMetric sales
    ) {
        BigDecimal momentum = ScoreMath.clamp(
            ScoreMath.NEUTRAL.add(valueOrZero(sales.growthRateQoq()))
        );
        return ScoreMath.clamp(
            demandScore.multiply(BigDecimal.valueOf(0.45))
                .add(momentum.multiply(BigDecimal.valueOf(0.35)))
                .add(
                    BigDecimal.valueOf(100).subtract(competitionScore)
                        .multiply(BigDecimal.valueOf(0.20))
                )
        );
    }

    private static BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
