package com.capstone.ai_insite.metric.domain;

import java.math.BigDecimal;

public record BuildingFeatureContext(
    Integer totalBuildingCount,
    Integer commercialBuildingCount,
    BigDecimal averageBuildingAge,
    BigDecimal agedBuildingRatio,
    BigDecimal averageGrossFloorArea,
    Integer totalParkingCount,
    BigDecimal parkingSpacesPerCommercialBuilding,
    BigDecimal commercialFloorAreaProxy,
    BigDecimal commercialFloorAreaRatio,
    BigDecimal physicalEnvironmentScore
) {

    public static BuildingFeatureContext empty() {
        return new BuildingFeatureContext(
            null, null, null, null, null, null, null, null, null, null
        );
    }

    public boolean hasAnyData() {
        return totalBuildingCount != null || physicalEnvironmentScore != null;
    }
}
