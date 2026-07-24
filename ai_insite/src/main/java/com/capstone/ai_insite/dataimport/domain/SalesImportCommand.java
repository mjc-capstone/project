package com.capstone.ai_insite.dataimport.domain;

public record SalesImportCommand(
    RawPayloadCommand rawPayload,
    String regionCode,
    String regionName,
    String categoryCode,
    String categoryName,
    String periodCode,
    String sourcePeriodCode,
    Long salesAmount,
    Long salesCount,
    Long weekdaySalesAmount,
    Long weekendSalesAmount,
    String salesByDayJson,
    String salesByTimeJson,
    String salesByDemographicJson,
    String sourceRowJson
) {
}
