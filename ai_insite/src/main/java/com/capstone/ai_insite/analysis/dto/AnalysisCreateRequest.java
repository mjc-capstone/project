package com.capstone.ai_insite.analysis.dto;

import com.capstone.ai_insite.analysis.domain.AnalysisCommand;
import com.capstone.ai_insite.analysis.domain.UserBusinessCondition;
import java.math.BigDecimal;
import tools.jackson.databind.JsonNode;

public record AnalysisCreateRequest(
    String regionCode,
    String categoryCode,
    String periodCode,
    String inputAddress,
    BigDecimal latitude,
    BigDecimal longitude,
    Long userBudget,
    Long userMaxRent,
    Long targetMonthlySales,
    BigDecimal preferredAreaSquareMeter,
    String operationType,
    Boolean franchise,
    JsonNode targetCustomer
) {
    public AnalysisCommand toCommand() {
        return new AnalysisCommand(
            regionCode,
            categoryCode,
            periodCode,
            inputAddress,
            latitude,
            longitude,
            new UserBusinessCondition(
                userBudget,
                userMaxRent,
                targetMonthlySales,
                preferredAreaSquareMeter,
                operationType,
                franchise,
                targetCustomer == null ? null : targetCustomer.toString()
            )
        );
    }
}
