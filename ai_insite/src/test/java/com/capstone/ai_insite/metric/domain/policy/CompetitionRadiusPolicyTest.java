package com.capstone.ai_insite.metric.domain.policy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CompetitionRadiusPolicyTest {

    private final CompetitionRadiusPolicy policy = new CompetitionRadiusPolicy();

    @Test
    void appliesThreeHundredMeterBoundary() {
        BigDecimal latitude = new BigDecimal("37.50000000");
        BigDecimal longitude = new BigDecimal("127.00000000");
        BigDecimal aboutThreeHundredMetersNorth = new BigDecimal("37.50270000");

        assertFalse(policy.isWithinMeters(
            latitude,
            longitude,
            aboutThreeHundredMetersNorth,
            longitude,
            300
        ));
        assertTrue(policy.isWithinMeters(
            latitude,
            longitude,
            aboutThreeHundredMetersNorth,
            longitude,
            301
        ));
    }

    @Test
    void appliesFiveHundredMeterBoundary() {
        BigDecimal latitude = new BigDecimal("37.50000000");
        BigDecimal longitude = new BigDecimal("127.00000000");
        BigDecimal aboutFiveHundredMetersNorth = new BigDecimal("37.50449000");

        assertFalse(policy.isWithinMeters(
            latitude,
            longitude,
            aboutFiveHundredMetersNorth,
            longitude,
            499
        ));
        assertTrue(policy.isWithinMeters(
            latitude,
            longitude,
            aboutFiveHundredMetersNorth,
            longitude,
            500
        ));
    }
}
