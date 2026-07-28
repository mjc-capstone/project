package com.capstone.ai_insite.region.domain.policy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class StoreRegionMappingPolicy {

    public Optional<Decision> resolve(
        Long directRegionId,
        List<Long> legalDongMappedRegionIds
    ) {
        if (directRegionId != null) {
            return Optional.of(new Decision(
                directRegionId,
                new BigDecimal("1.0000"),
                "EXACT_ADMINISTRATIVE_DONG_CODE"
            ));
        }
        List<Long> distinct = legalDongMappedRegionIds.stream().distinct().toList();
        if (distinct.size() == 1) {
            return Optional.of(new Decision(
                distinct.getFirst(),
                new BigDecimal("0.9000"),
                "UNIQUE_LEGAL_DONG_MAPPING"
            ));
        }
        return Optional.empty();
    }

    public record Decision(
        Long regionId,
        BigDecimal confidence,
        String rule
    ) {
    }
}
