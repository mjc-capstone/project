package com.capstone.ai_insite.region.domain.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class StoreRegionMappingPolicyTest {

    private final StoreRegionMappingPolicy policy = new StoreRegionMappingPolicy();

    @Test
    void directAdministrativeDongCodeHasPriority() {
        var decision = policy.resolve(10L, List.of(20L)).orElseThrow();

        assertEquals(10L, decision.regionId());
        assertEquals("EXACT_ADMINISTRATIVE_DONG_CODE", decision.rule());
    }

    @Test
    void ambiguousLegalDongMappingIsNotGuessed() {
        assertTrue(policy.resolve(null, List.of(10L, 20L)).isEmpty());
    }
}
