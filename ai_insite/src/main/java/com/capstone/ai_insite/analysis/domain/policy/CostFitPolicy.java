package com.capstone.ai_insite.analysis.domain.policy;

import com.capstone.ai_insite.analysis.domain.UserBusinessCondition;
import com.capstone.ai_insite.common.value.ScoreMath;
import com.capstone.ai_insite.metric.domain.CostFeatureContext;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CostFitPolicy {

    private static final BigDecimal THOUSAND_WON = BigDecimal.valueOf(1_000);

    public BigDecimal score(
        UserBusinessCondition condition,
        CostFeatureContext cost
    ) {
        BigDecimal affordability = affordability(condition, cost);
        BigDecimal burdenFit = cost.fixedCostBurdenIndex() == null
            ? null
            : BigDecimal.valueOf(100).subtract(cost.fixedCostBurdenIndex());
        if (affordability == null && burdenFit == null) {
            return ScoreMath.NEUTRAL.setScale(4, RoundingMode.HALF_UP);
        }
        if (affordability == null) {
            return ScoreMath.clamp(burdenFit);
        }
        if (burdenFit == null) {
            return ScoreMath.clamp(affordability);
        }
        return ScoreMath.weighted(affordability, 0.70, burdenFit, 0.30);
    }

    public BigDecimal estimatedMonthlyRent(
        UserBusinessCondition condition,
        CostFeatureContext cost
    ) {
        if (condition.preferredAreaSquareMeter() == null
            || condition.preferredAreaSquareMeter().signum() <= 0
            || cost.rentAmountPerSquareMeter() == null) {
            return null;
        }
        return cost.rentAmountPerSquareMeter()
            .multiply(THOUSAND_WON)
            .multiply(condition.preferredAreaSquareMeter())
            .setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal affordability(
        UserBusinessCondition condition,
        CostFeatureContext cost
    ) {
        BigDecimal estimatedRent = estimatedMonthlyRent(condition, cost);
        Long maxRent = condition.maxMonthlyRent();
        if (estimatedRent == null || maxRent == null || maxRent <= 0) {
            return null;
        }
        if (estimatedRent.compareTo(BigDecimal.valueOf(maxRent)) <= 0) {
            return BigDecimal.valueOf(100);
        }
        return BigDecimal.valueOf(maxRent)
            .multiply(BigDecimal.valueOf(100))
            .divide(estimatedRent, 4, RoundingMode.HALF_UP);
    }
}
