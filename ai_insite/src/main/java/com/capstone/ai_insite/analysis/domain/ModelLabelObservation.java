package com.capstone.ai_insite.analysis.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ModelLabelObservation(
    String periodCode,
    LocalDate startDate,
    Long salesAmount,
    Integer storeCount,
    BigDecimal closeRate
) {
}
