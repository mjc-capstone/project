package com.capstone.ai_insite.analysis.domain.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.capstone.ai_insite.analysis.domain.UserBusinessCondition;
import com.capstone.ai_insite.metric.domain.BuildingFeatureContext;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class BuildingFitPolicyTest {

    private final BuildingFitPolicy policy = new BuildingFitPolicy();

    @Test
    void combinesPhysicalEnvironmentAndPreferredAreaFit() {
        UserBusinessCondition condition = new UserBusinessCondition(
            null,
            null,
            null,
            new BigDecimal("200"),
            null,
            null,
            null
        );
        BuildingFeatureContext building = new BuildingFeatureContext(
            20,
            10,
            new BigDecimal("20"),
            new BigDecimal("30"),
            new BigDecimal("100"),
            50,
            new BigDecimal("5"),
            new BigDecimal("1000"),
            new BigDecimal("60"),
            new BigDecimal("80")
        );

        assertEquals(
            new BigDecimal("71.0000"),
            policy.score(condition, building)
        );
    }

    @Test
    void returnsNeutralWhenBuildingDataIsUnavailable() {
        UserBusinessCondition condition = new UserBusinessCondition(
            null, null, null, null, null, null, null
        );

        assertEquals(
            new BigDecimal("50.0000"),
            policy.score(condition, BuildingFeatureContext.empty())
        );
    }
}
