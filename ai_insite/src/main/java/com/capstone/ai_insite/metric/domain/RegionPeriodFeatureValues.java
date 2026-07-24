package com.capstone.ai_insite.metric.domain;

import java.math.BigDecimal;

public record RegionPeriodFeatureValues(
    Long floatingPopulationTotal,
    String floatingPopulationByAgeJson,
    String floatingPopulationByTimeJson,
    Long residentPopulationTotal,
    String residentPopulationByAgeJson,
    Long householdCount,
    Long workingPopulationTotal,
    String workingPopulationByAgeJson,
    Integer facilityTotalCount,
    String facilityDetailJson,
    Integer apartmentComplexCount,
    String apartmentDetailJson,
    BigDecimal daytimePopulationRatio,
    BigDecimal nightPopulationRatio,
    BigDecimal weekendPopulationRatio,
    BigDecimal residentialDemandScore,
    BigDecimal officeDemandScore,
    BigDecimal attractionScore,
    BigDecimal trafficAccessScore
) {
}
