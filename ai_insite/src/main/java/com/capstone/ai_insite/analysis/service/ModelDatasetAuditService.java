package com.capstone.ai_insite.analysis.service;

import com.capstone.ai_insite.analysis.domain.DatasetSplit;
import com.capstone.ai_insite.analysis.domain.ModelDatasetAudit;
import com.capstone.ai_insite.analysis.domain.ModelDatasetAuditStatus;
import com.capstone.ai_insite.analysis.domain.ModelDatasetFeatureAudit;
import com.capstone.ai_insite.analysis.domain.ModelDatasetLabelAudit;
import com.capstone.ai_insite.analysis.entity.ModelDatasetBuildEntity;
import com.capstone.ai_insite.analysis.entity.ModelDatasetMemberEntity;
import com.capstone.ai_insite.analysis.repository.ModelDatasetBuildJpaRepository;
import com.capstone.ai_insite.analysis.repository.ModelDatasetMemberJpaRepository;
import com.capstone.ai_insite.common.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class ModelDatasetAuditService {

    private static final int PAGE_SIZE = 500;
    private static final int MINIMUM_CONSECUTIVE_QUARTERS = 8;
    private static final List<String> LABEL_NAMES = List.of(
        "nextQuarterSalesGrowthRate",
        "nextQuarterStoreCountDeclined",
        "nextQuarterCloseRate",
        "fourQuarterStoreRetentionRate",
        "fourQuarterStoreBaseMaintained"
    );

    private final ModelDatasetBuildJpaRepository datasetRepository;
    private final ModelDatasetMemberJpaRepository memberRepository;
    private final ObjectMapper objectMapper;

    public ModelDatasetAuditService(
        ModelDatasetBuildJpaRepository datasetRepository,
        ModelDatasetMemberJpaRepository memberRepository,
        ObjectMapper objectMapper
    ) {
        this.datasetRepository = datasetRepository;
        this.memberRepository = memberRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public ModelDatasetAudit audit(Long datasetId) {
        ModelDatasetBuildEntity dataset = datasetRepository.findById(datasetId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "모델 데이터셋을 찾을 수 없습니다: " + datasetId
            ));
        AuditAccumulator audit = new AuditAccumulator();
        long lastId = 0L;
        Slice<ModelDatasetMemberEntity> members;
        do {
            members = memberRepository.findByDatasetBuildIdAndIdGreaterThanOrderByIdAsc(
                datasetId,
                lastId,
                PageRequest.of(0, PAGE_SIZE)
            );
            members.forEach(member -> accumulate(audit, member));
            if (members.hasContent()) {
                lastId = members.getContent().getLast().getId();
            }
        } while (members.hasNext());

        List<String> blockers = blockers(audit);
        List<String> warnings = warnings(audit);
        return new ModelDatasetAudit(
            dataset.getId(),
            dataset.getDatasetVersion(),
            dataset.getFeatureVersion(),
            dataset.getLabelVersion(),
            blockers.isEmpty()
                ? ModelDatasetAuditStatus.READY
                : ModelDatasetAuditStatus.NOT_READY,
            audit.total,
            audit.regions.size(),
            audit.categories.size(),
            audit.periods.isEmpty() ? null : audit.periods.firstEntry().getValue(),
            audit.periods.isEmpty() ? null : audit.periods.lastEntry().getValue(),
            audit.periods.size(),
            longestConsecutive(audit.periods.keySet()),
            Map.copyOf(audit.splitCounts),
            labelAudits(audit),
            featureAudits(audit),
            List.copyOf(blockers),
            List.copyOf(warnings),
            Instant.now()
        );
    }

    private void accumulate(
        AuditAccumulator audit,
        ModelDatasetMemberEntity member
    ) {
        var feature = member.getFeatureSnapshot();
        audit.splitCounts.merge(member.getDatasetSplit(), 1L, Long::sum);
        audit.regions.add(feature.getRegion().getAdministrativeDongCode());
        audit.categories.add(feature.getBusinessCategory().getSourceCategoryCode());
        audit.periods.put(
            feature.getMetricPeriod().getStartDate(),
            feature.getMetricPeriod().getPeriodCode()
        );
        try {
            JsonNode featureJson = objectMapper.readTree(feature.getFeatureJson());
            JsonNode labelJson = objectMapper.readTree(feature.getLabelJson());
            accumulateFeatures(audit, featureJson);
            accumulateLabels(audit, labelJson, member.getDatasetSplit());
            audit.total++;
        } catch (Exception exception) {
            throw new IllegalStateException(
                "모델 데이터셋 audit JSON 해석에 실패했습니다: " + feature.getId(),
                exception
            );
        }
    }

    private static void accumulateFeatures(
        AuditAccumulator audit,
        JsonNode features
    ) {
        Set<String> present = new HashSet<>();
        for (Map.Entry<String, JsonNode> property : features.properties()) {
            String name = property.getKey();
            JsonNode value = property.getValue();
            FeatureAccumulator accumulator = audit.features.computeIfAbsent(
                name,
                ignored -> new FeatureAccumulator(audit.total)
            );
            if (value == null || value.isNull()) {
                accumulator.missing++;
            } else {
                present.add(name);
            }
        }
        for (Map.Entry<String, FeatureAccumulator> entry : audit.features.entrySet()) {
            if (!features.has(entry.getKey())) {
                entry.getValue().missing++;
            }
        }
    }

    private static void accumulateLabels(
        AuditAccumulator audit,
        JsonNode labels,
        DatasetSplit split
    ) {
        for (String name : LABEL_NAMES) {
            LabelAccumulator accumulator = audit.labels.computeIfAbsent(
                name,
                ignored -> new LabelAccumulator()
            );
            JsonNode value = labels.get(name);
            if (value == null || value.isNull()) {
                accumulator.missing++;
                continue;
            }
            accumulator.available++;
            accumulator.availableBySplit.merge(split, 1L, Long::sum);
            if (value.isBoolean()) {
                if (value.asBoolean()) {
                    accumulator.trueCount++;
                } else {
                    accumulator.falseCount++;
                }
            }
        }
    }

    private static List<String> blockers(AuditAccumulator audit) {
        List<String> blockers = new ArrayList<>();
        if (audit.total == 0) {
            blockers.add("데이터셋에 학습 예제가 없습니다.");
            return blockers;
        }
        int consecutive = longestConsecutive(audit.periods.keySet());
        if (consecutive < MINIMUM_CONSECUTIVE_QUARTERS) {
            blockers.add(
                "연속 피처 분기가 " + consecutive + "개입니다. 최소 8개가 필요합니다."
            );
        }
        for (DatasetSplit split : DatasetSplit.values()) {
            if (audit.splitCounts.getOrDefault(split, 0L) == 0) {
                blockers.add(split + " 분할에 예제가 없습니다.");
            }
        }
        for (String name : LABEL_NAMES) {
            LabelAccumulator label = audit.labels.get(name);
            for (DatasetSplit split : DatasetSplit.values()) {
                if (label == null
                    || label.availableBySplit.getOrDefault(split, 0L) == 0) {
                    blockers.add(name + " 라벨이 " + split + " 분할에 없습니다.");
                }
            }
            if (label != null
                && label.trueCount + label.falseCount > 0
                && (label.trueCount == 0 || label.falseCount == 0)) {
                blockers.add(name + " 이진 라벨에 한 클래스만 존재합니다.");
            }
        }
        return blockers;
    }

    private static List<String> warnings(AuditAccumulator audit) {
        if (audit.total == 0) {
            return List.of();
        }
        List<String> warnings = new ArrayList<>();
        for (Map.Entry<String, FeatureAccumulator> entry : audit.features.entrySet()) {
            BigDecimal rate = rate(entry.getValue().missing, audit.total);
            if (rate.compareTo(BigDecimal.valueOf(50)) >= 0) {
                warnings.add(
                    entry.getKey() + " 피처 결측률이 " + rate.toPlainString() + "%입니다."
                );
            }
        }
        return warnings;
    }

    private static Map<String, ModelDatasetLabelAudit> labelAudits(
        AuditAccumulator audit
    ) {
        Map<String, ModelDatasetLabelAudit> result = new LinkedHashMap<>();
        for (String name : LABEL_NAMES) {
            LabelAccumulator value = audit.labels.getOrDefault(
                name,
                new LabelAccumulator()
            );
            boolean binary = value.trueCount + value.falseCount > 0;
            result.put(name, new ModelDatasetLabelAudit(
                value.available,
                value.missing,
                binary ? value.trueCount : null,
                binary ? value.falseCount : null,
                Map.copyOf(value.availableBySplit)
            ));
        }
        return Map.copyOf(result);
    }

    private static Map<String, ModelDatasetFeatureAudit> featureAudits(
        AuditAccumulator audit
    ) {
        Map<String, ModelDatasetFeatureAudit> result = new TreeMap<>();
        audit.features.forEach((name, value) -> result.put(
            name,
            new ModelDatasetFeatureAudit(
                value.missing,
                rate(value.missing, audit.total)
            )
        ));
        return Map.copyOf(result);
    }

    private static BigDecimal rate(long count, long total) {
        if (total == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(count)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);
    }

    private static int longestConsecutive(Set<LocalDate> periods) {
        int longest = 0;
        int current = 0;
        LocalDate previous = null;
        for (LocalDate period : periods.stream().sorted().toList()) {
            current = previous != null && previous.plusMonths(3).equals(period)
                ? current + 1
                : 1;
            longest = Math.max(longest, current);
            previous = period;
        }
        return longest;
    }

    private static final class AuditAccumulator {
        private long total;
        private final Set<String> regions = new HashSet<>();
        private final Set<String> categories = new HashSet<>();
        private final TreeMap<LocalDate, String> periods = new TreeMap<>();
        private final Map<DatasetSplit, Long> splitCounts =
            new EnumMap<>(DatasetSplit.class);
        private final Map<String, FeatureAccumulator> features =
            new LinkedHashMap<>();
        private final Map<String, LabelAccumulator> labels =
            new LinkedHashMap<>();
    }

    private static final class FeatureAccumulator {
        private long missing;

        private FeatureAccumulator(long missing) {
            this.missing = missing;
        }
    }

    private static final class LabelAccumulator {
        private long available;
        private long missing;
        private long trueCount;
        private long falseCount;
        private final Map<DatasetSplit, Long> availableBySplit =
            new EnumMap<>(DatasetSplit.class);
    }
}
