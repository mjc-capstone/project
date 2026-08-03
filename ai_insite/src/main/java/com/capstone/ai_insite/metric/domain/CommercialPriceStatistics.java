package com.capstone.ai_insite.metric.domain;

import java.math.BigDecimal;

public record CommercialPriceStatistics(
    int transactionCount,
    int pricedTransactionCount,
    BigDecimal medianPricePerArea,
    BigDecimal averagePricePerArea,
    BigDecimal pricePerAreaP25,
    BigDecimal pricePerAreaP75
) {
}
