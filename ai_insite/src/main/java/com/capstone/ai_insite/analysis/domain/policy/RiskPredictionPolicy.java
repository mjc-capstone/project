package com.capstone.ai_insite.analysis.domain.policy;

import com.capstone.ai_insite.analysis.domain.AnalysisPrediction;
import com.capstone.ai_insite.analysis.domain.UserBusinessCondition;
import com.capstone.ai_insite.common.value.ScoreMath;
import com.capstone.ai_insite.metric.domain.CommercialMetric;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class RiskPredictionPolicy {

    public AnalysisPrediction predict(
        CommercialMetric metric,
        UserBusinessCondition condition
    ) {
        BigDecimal inverseCompetition = BigDecimal.valueOf(100)
            .subtract(metric.scores().competitionScore());
        BigDecimal locationFit = ScoreMath.weighted(
            metric.scores().demandScore(),
            0.55,
            inverseCompetition,
            0.45
        );
        BigDecimal closureRisk = ScoreMath.weighted(
            metric.scores().closureRiskSignal(),
            0.60,
            BigDecimal.valueOf(100).subtract(metric.scores().stabilityScore()),
            0.40
        );
        BigDecimal targetFit = targetSalesFit(metric, condition.targetMonthlySales());
        BigDecimal success = ScoreMath.clamp(
            metric.scores().marketScore().multiply(BigDecimal.valueOf(0.40))
                .add(metric.scores().stabilityScore().multiply(BigDecimal.valueOf(0.25)))
                .add(locationFit.multiply(BigDecimal.valueOf(0.20)))
                .add(targetFit.multiply(BigDecimal.valueOf(0.15)))
        );

        var positives = new ArrayList<String>();
        var risks = new ArrayList<String>();
        classify("지역 수요가 양호합니다.", "지역 수요가 낮습니다.",
            metric.scores().demandScore(), positives, risks);
        classify("시장 성장성이 양호합니다.", "시장 성장성이 낮습니다.",
            metric.scores().marketScore(), positives, risks);
        classify("영업 안정성이 양호합니다.", "폐업 위험 신호를 주의해야 합니다.",
            metric.scores().stabilityScore(), positives, risks);
        if (targetFit.compareTo(BigDecimal.valueOf(70)) >= 0) {
            positives.add("목표 매출이 현재 상권의 점포당 매출 범위와 부합합니다.");
        } else if (condition.targetMonthlySales() != null) {
            risks.add("목표 매출이 현재 상권의 점포당 매출보다 높습니다.");
        }
        return new AnalysisPrediction(success, closureRisk, locationFit, positives, risks);
    }

    private static BigDecimal targetSalesFit(CommercialMetric metric, Long targetMonthlySales) {
        Long quarterlySales = metric.sales().salesAmount();
        Integer storeCount = metric.stores().storeCount();
        if (targetMonthlySales == null || targetMonthlySales <= 0
            || quarterlySales == null || storeCount == null || storeCount <= 0) {
            return ScoreMath.NEUTRAL;
        }
        BigDecimal monthlySalesPerStore = BigDecimal.valueOf(quarterlySales)
            .divide(BigDecimal.valueOf(storeCount * 3L), 4, RoundingMode.HALF_UP);
        BigDecimal gapRatio = BigDecimal.valueOf(targetMonthlySales)
            .subtract(monthlySalesPerStore)
            .abs()
            .divide(monthlySalesPerStore.max(BigDecimal.ONE), 4, RoundingMode.HALF_UP);
        return ScoreMath.clamp(
            BigDecimal.valueOf(100).subtract(gapRatio.multiply(BigDecimal.valueOf(100)))
        );
    }

    private static void classify(
        String positive,
        String risk,
        BigDecimal score,
        java.util.List<String> positives,
        java.util.List<String> risks
    ) {
        if (score.compareTo(BigDecimal.valueOf(65)) >= 0) {
            positives.add(positive);
        } else if (score.compareTo(BigDecimal.valueOf(40)) < 0) {
            risks.add(risk);
        }
    }
}
