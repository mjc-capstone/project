package com.capstone.ai_insite.metric.domain;

import java.math.BigDecimal;

public record CostFeatureContext(
    BigDecimal rentAmountPerSquareMeter,
    BigDecimal rentIndex,
    BigDecimal vacancyRate,
    BigDecimal investmentReturnRate,
    BigDecimal rentPressureScore,
    BigDecimal vacancyRiskScore,
    BigDecimal fixedCostBurdenIndex,
    Integer commercialTransactionCount,
    BigDecimal medianCommercialPricePerArea,
    BigDecimal priceGrowthRate,
    BigDecimal locationCostScore
) {

    public static CostFeatureContext empty() {
        return new CostFeatureContext(
            null, null, null, null, null, null,
            null, null, null, null, null
        );
    }

    public boolean hasAnyData() {
        return rentAmountPerSquareMeter != null
            || vacancyRate != null
            || medianCommercialPricePerArea != null;
    }
}
