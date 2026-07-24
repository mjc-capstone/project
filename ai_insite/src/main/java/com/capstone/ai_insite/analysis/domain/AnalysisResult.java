package com.capstone.ai_insite.analysis.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AnalysisResult(
    Long analysisId,
    String regionCode,
    String categoryCode,
    String periodCode,
    BigDecimal successScore,
    BigDecimal closureRiskScore,
    BigDecimal locationFitScore,
    RecommendationGrade recommendationGrade,
    List<String> positiveFactors,
    List<String> riskFactors,
    String summary,
    String featureVersion,
    LocalDateTime createdAt
) {
}
