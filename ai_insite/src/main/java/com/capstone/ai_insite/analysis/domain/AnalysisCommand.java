package com.capstone.ai_insite.analysis.domain;

import java.math.BigDecimal;

public record AnalysisCommand(
    String regionCode,
    String categoryCode,
    String periodCode,
    String inputAddress,
    BigDecimal latitude,
    BigDecimal longitude,
    UserBusinessCondition condition
) {
    public AnalysisCommand {
        if (regionCode == null || regionCode.isBlank()) {
            throw new IllegalArgumentException("regionCode는 필수입니다.");
        }
        if (categoryCode == null || categoryCode.isBlank()) {
            throw new IllegalArgumentException("categoryCode는 필수입니다.");
        }
        if (periodCode == null || periodCode.isBlank()) {
            throw new IllegalArgumentException("periodCode는 필수입니다.");
        }
    }
}
