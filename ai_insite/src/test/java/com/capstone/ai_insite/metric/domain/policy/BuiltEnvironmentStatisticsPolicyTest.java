package com.capstone.ai_insite.metric.domain.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.capstone.ai_insite.metric.domain.BuildingObservation;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class BuiltEnvironmentStatisticsPolicyTest {

    private final BuildingUsePolicy usePolicy = new BuildingUsePolicy();
    private final BuiltEnvironmentStatisticsPolicy policy =
        new BuiltEnvironmentStatisticsPolicy(usePolicy);

    @Test
    void identifiesCommercialUseCodes() {
        assertTrue(usePolicy.isCommercial("03000"));
        assertTrue(usePolicy.isCommercial("14000"));
        assertEquals(false, usePolicy.isCommercial("02000"));
    }

    @Test
    void calculatesCommercialAreaAgeAndParkingFeatures() {
        LocalDate snapshot = LocalDate.of(2026, 8, 3);
        var result = policy.calculate(List.of(
            new BuildingObservation(
                "03000",
                LocalDate.of(1990, 8, 3),
                new BigDecimal("100"),
                2
            ),
            new BuildingObservation(
                "14000",
                LocalDate.of(2016, 8, 3),
                new BigDecimal("300"),
                8
            ),
            new BuildingObservation(
                "02000",
                LocalDate.of(2020, 1, 1),
                new BigDecimal("200"),
                20
            )
        ), snapshot);

        assertEquals(3, result.totalBuildingCount());
        assertEquals(2, result.commercialBuildingCount());
        assertEquals(new BigDecimal("200.0000"), result.averageGrossFloorArea());
        assertEquals(10, result.totalParkingCount());
        assertEquals(
            new BigDecimal("5.0000"),
            result.parkingSpacesPerCommercialBuilding()
        );
        assertEquals(new BigDecimal("400"), result.commercialFloorAreaProxy());
        assertEquals(
            new BigDecimal("66.6667"),
            result.commercialFloorAreaRatio()
        );
        assertEquals(new BigDecimal("50.0000"), result.agedBuildingRatio());
    }
}
