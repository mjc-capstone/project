package com.capstone.ai_insite.dataimport.dto.reb;

import com.capstone.ai_insite.dataimport.domain.RebCommercialMetricType;
import com.capstone.ai_insite.dataimport.domain.RebCommercialPropertyType;
import java.math.BigDecimal;

public record RebCommercialRentObservation(
    String statisticTableId,
    String periodIdentifier,
    String sourceRegionCode,
    String sourceRegionName,
    String sourceRegionFullName,
    String regionLevel,
    RebCommercialPropertyType propertyType,
    RebCommercialMetricType metricType,
    BigDecimal value,
    String unitName,
    String sourceItemCode,
    String sourceItemName,
    String sourceRowJson
) {
}
