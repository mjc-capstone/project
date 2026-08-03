package com.capstone.ai_insite.dataimport.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SeoulQuarterTest {

    @Test
    void createsAnInclusiveRangeAcrossYears() {
        var quarters = SeoulQuarter.rangeInclusive("20234", "20242", 20);

        assertEquals(
            java.util.List.of("20234", "20241", "20242"),
            quarters.stream().map(SeoulQuarter::sourceCode).toList()
        );
    }

    @Test
    void rejectsReversedAndOversizedRanges() {
        assertThrows(
            IllegalArgumentException.class,
            () -> SeoulQuarter.rangeInclusive("20242", "20241", 20)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> SeoulQuarter.rangeInclusive("20201", "20261", 20)
        );
    }
}
