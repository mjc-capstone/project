package com.capstone.ai_insite.analysis.domain;

import java.math.BigDecimal;
import java.util.List;

public record AnalysisPrediction(
    BigDecimal successScore,
    BigDecimal closureRiskScore,
    BigDecimal locationFitScore,
    List<String> positiveFactors,
    List<String> riskFactors
) {
}
