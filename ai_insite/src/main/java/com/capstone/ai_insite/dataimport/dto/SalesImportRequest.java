package com.capstone.ai_insite.dataimport.dto;

import com.capstone.ai_insite.dataimport.domain.SalesImportCommand;
import tools.jackson.databind.JsonNode;

public record SalesImportRequest(
    RawPayloadRequest rawPayload,
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
    JsonNode salesByDay,
    JsonNode salesByTime,
    JsonNode salesByDemographic,
    JsonNode sourceRow
) {
    public SalesImportCommand toCommand() {
        return new SalesImportCommand(
            rawPayload.toCommand(),
            regionCode,
            regionName,
            categoryCode,
            categoryName,
            periodCode,
            sourcePeriodCode == null ? periodCode : sourcePeriodCode,
            salesAmount,
            salesCount,
            weekdaySalesAmount,
            weekendSalesAmount,
            json(salesByDay),
            json(salesByTime),
            json(salesByDemographic),
            json(sourceRow)
        );
    }

    private static String json(JsonNode node) {
        return node == null ? null : node.toString();
    }
}
