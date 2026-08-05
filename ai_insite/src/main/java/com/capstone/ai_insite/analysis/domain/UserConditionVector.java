package com.capstone.ai_insite.analysis.domain;

public record UserConditionVector(
    Double budget,
    Double maxMonthlyRent,
    Double targetMonthlySales,
    Double preferredAreaSquareMeter,
    String operationType,
    Boolean franchise
) {

    public static UserConditionVector empty() {
        return new UserConditionVector(null, null, null, null, null, null);
    }
}
