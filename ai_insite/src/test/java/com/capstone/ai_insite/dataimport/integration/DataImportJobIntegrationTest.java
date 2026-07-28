package com.capstone.ai_insite.dataimport.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.capstone.ai_insite.common.exception.ActiveDataImportJobException;
import com.capstone.ai_insite.dataimport.domain.DataImportJobProgress;
import com.capstone.ai_insite.dataimport.domain.DataImportJobStatus;
import com.capstone.ai_insite.dataimport.service.DataImportJobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DataImportJobIntegrationTest {

    @Autowired
    private DataImportJobService jobService;

    @Test
    void blocksConcurrentJobAndLinksRetryToFailedJob() {
        var first = jobService.start(
            "TEST_SOURCE",
            "TEST_SERVICE",
            "2099Q4",
            "integration-test",
            null
        );

        assertThrows(ActiveDataImportJobException.class, () -> jobService.start(
            "TEST_SOURCE",
            "TEST_SERVICE",
            "2099Q4",
            "integration-test",
            null
        ));
        jobService.fail(first.getId(), new IllegalStateException("page 3 failed"));

        var retry = jobService.start(
            "TEST_SOURCE",
            "TEST_SERVICE",
            "2099Q4",
            "integration-test",
            first.getId()
        );
        jobService.complete(retry.getId(), new DataImportJobProgress(0, 0, 0, 0));

        var completedRetry = jobService.get(retry.getId());
        assertEquals(DataImportJobStatus.COMPLETED, completedRetry.getStatus());
        assertEquals(first.getId(), completedRetry.getRetryOfJob().getId());
    }
}
