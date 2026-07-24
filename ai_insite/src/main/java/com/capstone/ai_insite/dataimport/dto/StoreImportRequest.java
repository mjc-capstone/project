package com.capstone.ai_insite.dataimport.dto;

import com.capstone.ai_insite.dataimport.domain.StoreImportCommand;
import tools.jackson.databind.JsonNode;
import java.math.BigDecimal;

public record StoreImportRequest(
    RawPayloadRequest rawPayload,
    String regionCode,
    String regionName,
    String categoryCode,
    String categoryName,
    String periodCode,
    String sourcePeriodCode,
    Integer storeCount,
    Integer normalStoreCount,
    Integer franchiseStoreCount,
    BigDecimal openRate,
    Integer openStoreCount,
    BigDecimal closeRate,
    Integer closeStoreCount,
    JsonNode sourceRow
) {
    public StoreImportCommand toCommand() {
        return new StoreImportCommand(
            rawPayload.toCommand(),
            regionCode,
            regionName,
            categoryCode,
            categoryName,
            periodCode,
            sourcePeriodCode == null ? periodCode : sourcePeriodCode,
            storeCount,
            normalStoreCount,
            franchiseStoreCount,
            openRate,
            openStoreCount,
            closeRate,
            closeStoreCount,
            sourceRow == null ? null : sourceRow.toString()
        );
    }
}
