package com.capstone.ai_insite.metric.dto;

import com.capstone.ai_insite.metric.domain.CommercialMetric;
import java.math.BigDecimal;

public record CommercialMetricResponse(
    Long snapshotId,
    String regionCode,
    String regionName,
    String categoryCode,
    String categoryName,
    String periodCode,
    Long salesAmount,
    Long salesCount,
    BigDecimal averageTicketAmount,
    Integer storeCount,
    BigDecimal salesGrowthRateQoq,
    BigDecimal storeGrowthRateQoq,
    Long floatingPopulation,
    Long residentPopulation,
    Long workingPopulation,
    BigDecimal demandScore,
    BigDecimal competitionScore,
    BigDecimal marketScore,
    BigDecimal stabilityScore,
    BigDecimal closureRiskSignal
) {
    public static CommercialMetricResponse from(CommercialMetric metric) {
        return new CommercialMetricResponse(
            metric.snapshotId(),
            metric.regionCode(),
            metric.regionName(),
            metric.categoryCode(),
            metric.categoryName(),
            metric.periodCode(),
            metric.sales().salesAmount(),
            metric.sales().salesCount(),
            metric.sales().averageTicketAmount(),
            metric.stores().storeCount(),
            metric.sales().growthRateQoq(),
            metric.stores().growthRateQoq(),
            metric.demand().floatingPopulation(),
            metric.demand().residentPopulation(),
            metric.demand().workingPopulation(),
            metric.scores().demandScore(),
            metric.scores().competitionScore(),
            metric.scores().marketScore(),
            metric.scores().stabilityScore(),
            metric.scores().closureRiskSignal()
        );
    }
}
