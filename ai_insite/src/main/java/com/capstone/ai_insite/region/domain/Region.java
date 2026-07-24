package com.capstone.ai_insite.region.domain;

import java.math.BigDecimal;

public record Region(
    Long id,
    String administrativeDongCode,
    String sidoName,
    String sigunguName,
    String administrativeDongName,
    BigDecimal latitude,
    BigDecimal longitude
) {
}
