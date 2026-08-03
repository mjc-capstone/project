package com.capstone.ai_insite.analysis.domain.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.capstone.ai_insite.analysis.domain.ModelLabelObservation;
import com.capstone.ai_insite.analysis.domain.ModelLabelStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class NextQuarterLabelPolicyTest {

    private final NextQuarterLabelPolicy policy = new NextQuarterLabelPolicy();

    @Test
    void calculatesNextQuarterAndFourQuarterLabels() {
        var result = policy.calculate(
            observation("2025Q1", LocalDate.of(2025, 1, 1), 1_000L, 10, "2.0"),
            observation("2025Q2", LocalDate.of(2025, 4, 1), 1_200L, 8, "7.5"),
            observation("2026Q1", LocalDate.of(2026, 1, 1), 1_300L, 9, "4.0")
        );

        assertEquals(ModelLabelStatus.READY, result.status());
        assertEquals(
            new BigDecimal("20.0000"),
            result.values().nextQuarterSalesGrowthRate()
        );
        assertTrue(result.values().nextQuarterStoreCountDeclined());
        assertEquals(new BigDecimal("7.5"), result.values().nextQuarterCloseRate());
        assertEquals(
            new BigDecimal("90.0000"),
            result.values().fourQuarterStoreRetentionRate()
        );
        assertFalse(result.values().fourQuarterStoreBaseMaintained());
    }

    @Test
    void marksIncompleteSourceWithoutManufacturingAValue() {
        var result = policy.calculate(
            observation("2025Q1", LocalDate.of(2025, 1, 1), 0L, 10, "2.0"),
            observation("2025Q2", LocalDate.of(2025, 4, 1), 1_200L, 8, "7.5"),
            null
        );

        assertEquals(ModelLabelStatus.INCOMPLETE_SOURCE, result.status());
        assertEquals(null, result.values());
    }

    @Test
    void rejectsAQuarterGap() {
        assertThrows(IllegalArgumentException.class, () -> policy.calculate(
            observation("2025Q1", LocalDate.of(2025, 1, 1), 1_000L, 10, "2.0"),
            observation("2025Q3", LocalDate.of(2025, 7, 1), 1_200L, 8, "7.5"),
            null
        ));
    }

    private static ModelLabelObservation observation(
        String code,
        LocalDate start,
        Long sales,
        Integer stores,
        String closeRate
    ) {
        return new ModelLabelObservation(
            code,
            start,
            sales,
            stores,
            new BigDecimal(closeRate)
        );
    }
}
