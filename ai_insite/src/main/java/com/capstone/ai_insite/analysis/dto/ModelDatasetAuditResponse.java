package com.capstone.ai_insite.analysis.dto;

import com.capstone.ai_insite.analysis.domain.DatasetSplit;
import com.capstone.ai_insite.analysis.domain.ModelDatasetAudit;
import com.capstone.ai_insite.analysis.domain.ModelDatasetAuditStatus;
import com.capstone.ai_insite.analysis.domain.ModelDatasetFeatureAudit;
import com.capstone.ai_insite.analysis.domain.ModelDatasetLabelAudit;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record ModelDatasetAuditResponse(
    Long datasetId,
    String datasetVersion,
    String featureVersion,
    String labelVersion,
    ModelDatasetAuditStatus status,
    long totalExampleCount,
    int regionCount,
    int categoryCount,
    String firstFeaturePeriod,
    String lastFeaturePeriod,
    int distinctFeaturePeriodCount,
    int longestConsecutiveQuarterCount,
    Map<DatasetSplit, Long> splitCounts,
    Map<String, LabelAuditResponse> labels,
    Map<String, FeatureAuditResponse> features,
    List<String> blockers,
    List<String> warnings,
    Instant generatedAt
) {

    public static ModelDatasetAuditResponse from(ModelDatasetAudit audit) {
        Map<String, LabelAuditResponse> labels = new LinkedHashMap<>();
        audit.labels().forEach((name, value) -> labels.put(
            name,
            LabelAuditResponse.from(value)
        ));
        Map<String, FeatureAuditResponse> features = new LinkedHashMap<>();
        audit.features().forEach((name, value) -> features.put(
            name,
            FeatureAuditResponse.from(value)
        ));
        return new ModelDatasetAuditResponse(
            audit.datasetId(),
            audit.datasetVersion(),
            audit.featureVersion(),
            audit.labelVersion(),
            audit.status(),
            audit.totalExampleCount(),
            audit.regionCount(),
            audit.categoryCount(),
            audit.firstFeaturePeriod(),
            audit.lastFeaturePeriod(),
            audit.distinctFeaturePeriodCount(),
            audit.longestConsecutiveQuarterCount(),
            audit.splitCounts(),
            Map.copyOf(labels),
            Map.copyOf(features),
            audit.blockers(),
            audit.warnings(),
            audit.generatedAt()
        );
    }

    public record LabelAuditResponse(
        long availableCount,
        long missingCount,
        Long trueCount,
        Long falseCount,
        Map<DatasetSplit, Long> availableBySplit
    ) {
        private static LabelAuditResponse from(ModelDatasetLabelAudit audit) {
            return new LabelAuditResponse(
                audit.availableCount(),
                audit.missingCount(),
                audit.trueCount(),
                audit.falseCount(),
                audit.availableBySplit()
            );
        }
    }

    public record FeatureAuditResponse(
        long missingCount,
        BigDecimal missingRate
    ) {
        private static FeatureAuditResponse from(ModelDatasetFeatureAudit audit) {
            return new FeatureAuditResponse(
                audit.missingCount(),
                audit.missingRate()
            );
        }
    }
}
