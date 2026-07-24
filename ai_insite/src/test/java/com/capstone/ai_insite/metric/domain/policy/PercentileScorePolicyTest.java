package com.capstone.ai_insite.metric.domain.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PercentileScorePolicyTest {

    private final PercentileScorePolicy policy = new PercentileScorePolicy();

    @Test
    void scoresRelativePositionAndUsesAverageRankForTies() {
        Map<String, BigDecimal> values = new LinkedHashMap<>();
        values.put("LOW", BigDecimal.TEN);
        values.put("MIDDLE_A", BigDecimal.valueOf(20));
        values.put("MIDDLE_B", BigDecimal.valueOf(20));
        values.put("HIGH", BigDecimal.valueOf(30));
        values.put("MISSING", null);

        Map<String, BigDecimal> scores = policy.score(values);

        assertEquals(new BigDecimal("0.0000"), scores.get("LOW"));
        assertEquals(new BigDecimal("50.0000"), scores.get("MIDDLE_A"));
        assertEquals(new BigDecimal("50.0000"), scores.get("MIDDLE_B"));
        assertEquals(new BigDecimal("100.0000"), scores.get("HIGH"));
        assertEquals(false, scores.containsKey("MISSING"));
    }
}
