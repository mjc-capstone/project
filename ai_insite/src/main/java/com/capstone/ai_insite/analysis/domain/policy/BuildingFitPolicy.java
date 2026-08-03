package com.capstone.ai_insite.analysis.domain.policy;

import com.capstone.ai_insite.analysis.domain.UserBusinessCondition;
import com.capstone.ai_insite.common.value.ScoreMath;
import com.capstone.ai_insite.metric.domain.BuildingFeatureContext;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class BuildingFitPolicy {

    public BigDecimal score(
        UserBusinessCondition condition,
        BuildingFeatureContext building
    ) {
        BigDecimal environment = building.physicalEnvironmentScore();
        BigDecimal area = areaFit(condition, building);
        if (environment == null && area == null) {
            return ScoreMath.NEUTRAL.setScale(4, RoundingMode.HALF_UP);
        }
        if (environment == null) {
            return ScoreMath.clamp(area);
        }
        if (area == null) {
            return ScoreMath.clamp(environment);
        }
        return ScoreMath.weighted(environment, 0.70, area, 0.30);
    }

    private BigDecimal areaFit(
        UserBusinessCondition condition,
        BuildingFeatureContext building
    ) {
        BigDecimal preferred = condition.preferredAreaSquareMeter();
        BigDecimal available = building.averageGrossFloorArea();
        if (preferred == null || preferred.signum() <= 0
            || available == null || available.signum() <= 0) {
            return null;
        }
        if (available.compareTo(preferred) >= 0) {
            return BigDecimal.valueOf(100);
        }
        return available.multiply(BigDecimal.valueOf(100))
            .divide(preferred, 4, RoundingMode.HALF_UP);
    }
}
