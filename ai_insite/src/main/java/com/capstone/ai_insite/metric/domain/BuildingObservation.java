package com.capstone.ai_insite.metric.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BuildingObservation(
    String mainUseCode,
    LocalDate approvalDate,
    BigDecimal grossFloorArea,
    int parkingCount
) {
}
