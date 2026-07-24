package com.capstone.ai_insite.metric.domain;

import java.math.BigDecimal;

public record MetricScores(
    BigDecimal demandScore,
    BigDecimal competitionScore,
    BigDecimal marketScore,
    BigDecimal stabilityScore,
    BigDecimal closureRiskSignal
) {
}
