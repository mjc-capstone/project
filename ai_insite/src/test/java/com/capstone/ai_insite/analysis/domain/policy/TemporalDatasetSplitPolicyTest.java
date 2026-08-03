package com.capstone.ai_insite.analysis.domain.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.capstone.ai_insite.analysis.domain.DatasetSplit;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TemporalDatasetSplitPolicyTest {

    private final TemporalDatasetSplitPolicy policy =
        new TemporalDatasetSplitPolicy();

    @Test
    void assignsByLabelPeriodInsteadOfFeaturePeriod() {
        DatasetSplit split = policy.assign(
            LocalDate.of(2025, 3, 31),
            LocalDate.of(2025, 4, 1),
            LocalDate.of(2025, 6, 30),
            LocalDate.of(2025, 6, 30),
            LocalDate.of(2025, 9, 30),
            LocalDate.of(2025, 12, 31)
        );

        assertEquals(DatasetSplit.TRAIN, split);
    }

    @Test
    void rejectsFeatureDataFromInsideTheLabelPeriod() {
        assertThrows(IllegalArgumentException.class, () -> policy.assign(
            LocalDate.of(2025, 4, 15),
            LocalDate.of(2025, 4, 1),
            LocalDate.of(2025, 6, 30),
            LocalDate.of(2025, 6, 30),
            LocalDate.of(2025, 9, 30),
            LocalDate.of(2025, 12, 31)
        ));
    }

    @Test
    void rejectsOverlappingOrReversedBoundaries() {
        assertThrows(IllegalArgumentException.class, () ->
            policy.validateBoundaries(
                LocalDate.of(2025, 9, 30),
                LocalDate.of(2025, 9, 30),
                LocalDate.of(2025, 12, 31)
            )
        );
    }
}
