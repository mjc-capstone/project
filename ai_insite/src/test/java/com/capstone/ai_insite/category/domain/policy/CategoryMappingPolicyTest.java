package com.capstone.ai_insite.category.domain.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class CategoryMappingPolicyTest {

    private final CategoryMappingPolicy policy = new CategoryMappingPolicy();

    @Test
    void explicitCodeMappingWinsOverNameMapping() {
        var decision = policy.resolve(
            "Coffee",
            List.of(new CategoryMappingPolicy.ExplicitCandidate(
                20L,
                new BigDecimal("0.9500"),
                "SMALL_CATEGORY_CODE"
            )),
            List.of(new CategoryMappingPolicy.NameCandidate(10L, "Coffee"))
        ).orElseThrow();

        assertEquals(20L, decision.categoryId());
        assertEquals(new BigDecimal("0.9500"), decision.confidence());
        assertEquals("SMALL_CATEGORY_CODE", decision.rule());
    }

    @Test
    void exactNormalizedNameIsUsedWhenCodeMappingIsAbsent() {
        var decision = policy.resolve(
            "Coffee Shop",
            List.of(),
            List.of(new CategoryMappingPolicy.NameCandidate(10L, "coffee-shop"))
        ).orElseThrow();

        assertEquals(10L, decision.categoryId());
        assertEquals("EXACT_NORMALIZED_NAME", decision.rule());
    }
}
