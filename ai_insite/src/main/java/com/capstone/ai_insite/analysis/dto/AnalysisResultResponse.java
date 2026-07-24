package com.capstone.ai_insite.analysis.dto;

import com.capstone.ai_insite.analysis.domain.AnalysisResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AnalysisResultResponse(
    Long analysisId,
    String regionCode,
    String categoryCode,
    String periodCode,
    BigDecimal successScore,
    BigDecimal closureRiskScore,
    BigDecimal locationFitScore,
    String recommendationGrade,
    List<String> positiveFactors,
    List<String> riskFactors,
    String summary,
    String featureVersion,
    LocalDateTime createdAt
) {
    public static AnalysisResultResponse from(AnalysisResult result) {
        return new AnalysisResultResponse(
            result.analysisId(),
            result.regionCode(),
            result.categoryCode(),
            result.periodCode(),
            result.successScore(),
            result.closureRiskScore(),
            result.locationFitScore(),
            result.recommendationGrade().name(),
            result.positiveFactors(),
            result.riskFactors(),
            result.summary(),
            result.featureVersion(),
            result.createdAt()
        );
    }
}
