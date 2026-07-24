package com.capstone.ai_insite.metric.domain;

import java.math.BigDecimal;

public record DemandMetric(
    Long floatingPopulation,
    Long residentPopulation,
    Long workingPopulation,
    BigDecimal residentialDemandScore,
    BigDecimal officeDemandScore,
    BigDecimal attractionScore,
    BigDecimal trafficAccessScore
) {
    public static DemandMetric empty() {
        return new DemandMetric(null, null, null, null, null, null, null);
    }
}
