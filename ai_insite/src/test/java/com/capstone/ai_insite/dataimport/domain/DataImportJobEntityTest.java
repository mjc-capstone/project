package com.capstone.ai_insite.dataimport.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.capstone.ai_insite.dataimport.entity.DataImportJobEntity;
import org.junit.jupiter.api.Test;

class DataImportJobEntityTest {

    @Test
    void transitionsFromPendingToRunningToCompletedWithProgress() {
        DataImportJobEntity job = DataImportJobEntity.pending(
            "PUBLIC_DATA_PORTAL",
            "storeListInDong:11110",
            "202603",
            "test",
            null
        );

        job.start();
        job.record(new DataImportJobProgress(1, 100, 98, 2));
        job.complete(new DataImportJobProgress(2, 150, 148, 2));

        assertEquals(DataImportJobStatus.COMPLETED, job.getStatus());
        assertEquals(2, job.getTotalPageCount());
        assertEquals(150, job.getFetchedRowCount());
        assertNotNull(job.getCompletedAt());
    }

    @Test
    void rejectsInconsistentCounters() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new DataImportJobProgress(1, 10, 8, 1)
        );
    }
}
