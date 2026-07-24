package com.capstone.ai_insite.dataimport.domain;

import java.math.BigDecimal;

public record StoreImportCommand(
    RawPayloadCommand rawPayload,
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
    String sourceRowJson
) {
}
