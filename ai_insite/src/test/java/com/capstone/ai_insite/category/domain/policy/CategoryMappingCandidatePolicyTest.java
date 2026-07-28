package com.capstone.ai_insite.category.domain.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.capstone.ai_insite.category.domain.MappingStatus;
import java.util.List;
import org.junit.jupiter.api.Test;

class CategoryMappingCandidatePolicyTest {

    private final CategoryMappingCandidatePolicy policy =
        new CategoryMappingCandidatePolicy();

    @Test
    void exactCanonicalNameCanBeAutoConfirmed() {
        var decision = policy.propose(
            "한식",
            List.of(
                new CategoryMappingCandidatePolicy.Target(1L, "일식"),
                new CategoryMappingCandidatePolicy.Target(2L, "한식")
            )
        );

        assertEquals(2L, decision.categoryId());
        assertEquals(MappingStatus.AUTO_CONFIRMED, decision.status());
        assertEquals("EXACT_CANONICAL_NAME", decision.rule());
    }

    @Test
    void similarityCreatesCandidateButNeverConfirmedMapping() {
        var decision = policy.propose(
            "커피 전문점",
            List.of(new CategoryMappingCandidatePolicy.Target(10L, "커피 전문"))
        );

        assertEquals(MappingStatus.CANDIDATE, decision.status());
    }

    @Test
    void weakSimilarityRemainsUnresolved() {
        var decision = policy.propose(
            "자동차 수리",
            List.of(new CategoryMappingCandidatePolicy.Target(10L, "한식"))
        );

        assertEquals(MappingStatus.UNRESOLVED, decision.status());
        assertNull(decision.categoryId());
    }
}
