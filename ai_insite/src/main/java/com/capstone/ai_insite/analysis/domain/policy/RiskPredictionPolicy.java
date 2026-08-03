package com.capstone.ai_insite.analysis.domain.policy;

import com.capstone.ai_insite.analysis.domain.AnalysisPrediction;
import com.capstone.ai_insite.analysis.domain.UserBusinessCondition;
import com.capstone.ai_insite.common.value.ScoreMath;
import com.capstone.ai_insite.metric.domain.CommercialMetric;
import com.capstone.ai_insite.metric.domain.BuildingFeatureContext;
import com.capstone.ai_insite.metric.domain.CostFeatureContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class RiskPredictionPolicy {

    public AnalysisPrediction predict(
        CommercialMetric metric,
        UserBusinessCondition condition
    ) {
        return predictInternal(
            metric,
            condition,
            CostFeatureContext.empty(),
            BuildingFeatureContext.empty(),
            false,
            false
        );
    }

    public AnalysisPrediction predict(
        CommercialMetric metric,
        UserBusinessCondition condition,
        CostFeatureContext cost
    ) {
        return predictInternal(
            metric,
            condition,
            cost,
            BuildingFeatureContext.empty(),
            true,
            false
        );
    }

    public AnalysisPrediction predict(
        CommercialMetric metric,
        UserBusinessCondition condition,
        CostFeatureContext cost,
        BuildingFeatureContext building
    ) {
        return predictInternal(metric, condition, cost, building, true, true);
    }

    private AnalysisPrediction predictInternal(
        CommercialMetric metric,
        UserBusinessCondition condition,
        CostFeatureContext cost,
        BuildingFeatureContext building,
        boolean applyCost,
        boolean applyBuilding
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
        BigDecimal costFit = new CostFitPolicy().score(condition, cost);
        BigDecimal buildingFit = new BuildingFitPolicy().score(
            condition,
            building
        );
        if (applyCost && cost.locationCostScore() != null) {
            locationFit = ScoreMath.weighted(
                locationFit,
                0.75,
                BigDecimal.valueOf(100).subtract(cost.locationCostScore()),
                0.25
            );
        }
        if (applyCost && cost.fixedCostBurdenIndex() != null) {
            closureRisk = ScoreMath.weighted(
                closureRisk,
                0.75,
                cost.fixedCostBurdenIndex(),
                0.25
            );
        }
        if (applyBuilding && building.physicalEnvironmentScore() != null) {
            locationFit = ScoreMath.weighted(
                locationFit,
                0.85,
                building.physicalEnvironmentScore(),
                0.15
            );
        }
        if (applyBuilding && building.agedBuildingRatio() != null) {
            closureRisk = ScoreMath.weighted(
                closureRisk,
                0.85,
                building.agedBuildingRatio(),
                0.15
            );
        }
        BigDecimal success = applyBuilding
            ? ScoreMath.clamp(
                metric.scores().marketScore().multiply(BigDecimal.valueOf(0.30))
                    .add(metric.scores().stabilityScore().multiply(BigDecimal.valueOf(0.20)))
                    .add(locationFit.multiply(BigDecimal.valueOf(0.15)))
                    .add(targetFit.multiply(BigDecimal.valueOf(0.15)))
                    .add(costFit.multiply(BigDecimal.valueOf(0.10)))
                    .add(buildingFit.multiply(BigDecimal.valueOf(0.10)))
            )
            : applyCost
            ? ScoreMath.clamp(
                metric.scores().marketScore().multiply(BigDecimal.valueOf(0.35))
                    .add(metric.scores().stabilityScore().multiply(BigDecimal.valueOf(0.20)))
                    .add(locationFit.multiply(BigDecimal.valueOf(0.15)))
                    .add(targetFit.multiply(BigDecimal.valueOf(0.15)))
                    .add(costFit.multiply(BigDecimal.valueOf(0.15)))
            )
            : ScoreMath.clamp(
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
        if (applyCost && cost.hasAnyData()) {
            BigDecimal estimatedRent = new CostFitPolicy()
                .estimatedMonthlyRent(condition, cost);
            if (estimatedRent != null && condition.maxMonthlyRent() != null) {
                if (estimatedRent.compareTo(
                    BigDecimal.valueOf(condition.maxMonthlyRent())
                ) <= 0) {
                    positives.add("예상 월 임대료가 사용자의 허용 범위 안에 있습니다.");
                } else {
                    risks.add("예상 월 임대료가 사용자의 허용 범위를 초과합니다.");
                }
            }
            if (costFit.compareTo(BigDecimal.valueOf(65)) >= 0) {
                positives.add("입지 비용 부담이 사용자 조건과 부합합니다.");
            } else if (costFit.compareTo(BigDecimal.valueOf(40)) < 0) {
                risks.add("입지 비용 부담이 사용자 조건보다 높습니다.");
            }
        }
        if (applyBuilding && building.hasAnyData()) {
            if (buildingFit.compareTo(BigDecimal.valueOf(65)) >= 0) {
                positives.add("건축환경과 선호 면적이 사용자 조건에 부합합니다.");
            } else if (buildingFit.compareTo(BigDecimal.valueOf(40)) < 0) {
                risks.add("건축환경 또는 가용 면적 조건이 부족합니다.");
            }
            if (building.agedBuildingRatio() != null
                && building.agedBuildingRatio().compareTo(
                    BigDecimal.valueOf(60)
                ) >= 0) {
                risks.add("노후 건축물 비중이 높아 시설 상태 확인이 필요합니다.");
            }
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
