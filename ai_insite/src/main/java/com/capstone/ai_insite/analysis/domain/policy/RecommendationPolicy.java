package com.capstone.ai_insite.analysis.domain.policy;

import com.capstone.ai_insite.analysis.domain.AnalysisPrediction;
import com.capstone.ai_insite.analysis.domain.RecommendationGrade;
import java.math.BigDecimal;

public class RecommendationPolicy {

    public RecommendationGrade grade(AnalysisPrediction prediction) {
        BigDecimal score = prediction.successScore()
            .subtract(prediction.closureRiskScore().multiply(BigDecimal.valueOf(0.2)));
        if (score.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return RecommendationGrade.A;
        }
        if (score.compareTo(BigDecimal.valueOf(55)) >= 0) {
            return RecommendationGrade.B;
        }
        if (score.compareTo(BigDecimal.valueOf(40)) >= 0) {
            return RecommendationGrade.C;
        }
        return RecommendationGrade.D;
    }
}
