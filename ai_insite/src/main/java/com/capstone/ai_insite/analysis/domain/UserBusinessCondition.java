package com.capstone.ai_insite.analysis.domain;

import java.math.BigDecimal;

public record UserBusinessCondition(
    Long budget,
    Long maxMonthlyRent,
    Long targetMonthlySales,
    BigDecimal preferredAreaSquareMeter,
    String operationType,
    Boolean franchise,
    String targetCustomerJson
) {
}
