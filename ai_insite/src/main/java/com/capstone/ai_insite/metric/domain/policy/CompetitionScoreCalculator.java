package com.capstone.ai_insite.metric.domain.policy;

import com.capstone.ai_insite.common.value.ScoreMath;
import com.capstone.ai_insite.metric.domain.StoreMetric;
import java.math.BigDecimal;

public class CompetitionScoreCalculator {

    public BigDecimal calculate(StoreMetric stores) {
        BigDecimal franchisePressure = valueOrNeutral(stores.franchiseRatio());
        BigDecimal netOpeningPressure = ScoreMath.clamp(
            ScoreMath.NEUTRAL.add(valueOrZero(stores.openRate()))
                .subtract(valueOrZero(stores.closeRate()))
        );
        return ScoreMath.weighted(franchisePressure, 0.4, netOpeningPressure, 0.6);
    }

    private static BigDecimal valueOrNeutral(BigDecimal value) {
        return value == null ? ScoreMath.NEUTRAL : ScoreMath.clamp(value);
    }

    private static BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
