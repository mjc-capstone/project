package com.capstone.ai_insite.metric.service;

import com.capstone.ai_insite.metric.domain.CommercialMetric;
import com.capstone.ai_insite.metric.domain.DemandMetric;
import com.capstone.ai_insite.metric.domain.MetricScores;
import com.capstone.ai_insite.metric.domain.SalesMetric;
import com.capstone.ai_insite.metric.domain.StoreMetric;
import com.capstone.ai_insite.metric.entity.CommercialMetricSnapshotEntity;
import com.capstone.ai_insite.metric.entity.RegionPeriodFeatureEntity;

final class CommercialMetricMapper {

    private CommercialMetricMapper() {
    }

    static CommercialMetric toDomain(
        CommercialMetricSnapshotEntity entity,
        RegionPeriodFeatureEntity feature
    ) {
        return new CommercialMetric(
            entity.getId(),
            entity.getRegion().getAdministrativeDongCode(),
            entity.getRegion().getAdministrativeDongName(),
            entity.getBusinessCategory().getSourceCategoryCode(),
            entity.getBusinessCategory().getSourceCategoryName(),
            entity.getMetricPeriod().getPeriodCode(),
            new SalesMetric(
                entity.getSalesAmount(),
                entity.getSalesCount(),
                entity.getAvgTicketAmount(),
                entity.getSalesGrowthRateQoq(),
                entity.getSalesGrowthRateYoy()
            ),
            new StoreMetric(
                entity.getStoreCount(),
                entity.getOpenStoreCount(),
                entity.getCloseStoreCount(),
                entity.getOpenRate(),
                entity.getCloseRate(),
                entity.getFranchiseRatio(),
                entity.getStoreGrowthRateQoq()
            ),
            toDemand(feature),
            new MetricScores(
                entity.getDemandScore(),
                entity.getCompetitionIntensityScore(),
                entity.getMarketScore(),
                entity.getStabilityScore(),
                entity.getClosureRiskSignal()
            )
        );
    }

    static DemandMetric toDemand(RegionPeriodFeatureEntity feature) {
        if (feature == null) {
            return DemandMetric.empty();
        }
        return new DemandMetric(
            feature.getFloatingPopulationTotal(),
            feature.getResidentPopulationTotal(),
            feature.getWorkingPopulationTotal(),
            feature.getResidentialDemandScore(),
            feature.getOfficeDemandScore(),
            feature.getAttractionScore(),
            feature.getTrafficAccessScore()
        );
    }
}
