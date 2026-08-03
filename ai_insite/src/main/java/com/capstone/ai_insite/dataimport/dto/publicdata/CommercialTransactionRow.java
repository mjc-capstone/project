package com.capstone.ai_insite.dataimport.dto.publicdata;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CommercialTransactionRow(
    String districtCode,
    String districtName,
    String legalDongName,
    LocalDate dealDate,
    BigDecimal dealAmountKrw,
    BigDecimal buildingAreaSquareMeter,
    BigDecimal landAreaSquareMeter,
    String buildingType,
    String buildingUse,
    String landUse,
    Integer floor,
    Integer builtYear,
    String lotNumberMasked,
    boolean cancelled,
    String cancellationDay,
    String dealingType,
    String sourceRowJson
) {
}
