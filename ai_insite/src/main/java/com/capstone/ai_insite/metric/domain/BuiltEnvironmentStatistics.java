package com.capstone.ai_insite.metric.domain;

import java.math.BigDecimal;

public record BuiltEnvironmentStatistics(
    int totalBuildingCount,
    int commercialBuildingCount,
    BigDecimal averageBuildingAge,
    BigDecimal agedBuildingRatio,
    BigDecimal averageGrossFloorArea,
    int totalParkingCount,
    BigDecimal parkingSpacesPerCommercialBuilding,
    BigDecimal commercialFloorAreaProxy,
    BigDecimal commercialFloorAreaRatio,
    int sourceBuildingCount
) {
}
