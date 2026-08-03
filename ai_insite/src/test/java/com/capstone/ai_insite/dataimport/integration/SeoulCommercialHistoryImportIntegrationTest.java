package com.capstone.ai_insite.dataimport.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.capstone.ai_insite.dataimport.domain.SeoulQuarter;
import com.capstone.ai_insite.dataimport.service.SeoulCommercialHistoryImportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "external.seoul.enabled=true",
    "external.seoul.scheduler-enabled=false",
    "external.seoul.page-size=1000",
    "external.seoul.max-pages=100"
})
@EnabledIfEnvironmentVariable(
    named = "RUN_SEOUL_HISTORY_API_INTEGRATION",
    matches = "true"
)
class SeoulCommercialHistoryImportIntegrationTest {

    @Autowired
    private SeoulCommercialHistoryImportService importService;

    @Test
    void importsEveryQuarterInTheConfiguredRange() {
        String from = System.getenv().getOrDefault("SEOUL_HISTORY_FROM", "20232");
        String to = System.getenv().getOrDefault("SEOUL_HISTORY_TO", "20261");

        var result = importService.importRange(from, to);

        int expected = SeoulQuarter.rangeInclusive(from, to, 20).size();
        assertEquals(expected, result.quarterCount());
        assertEquals(expected, result.quarters().size());
        assertTrue(result.totalSalesRowCount() > 0);
        assertTrue(result.totalStoresRowCount() > 0);
        assertTrue(result.totalMetricSnapshotCount() > 0);
        result.quarters().forEach(quarter -> {
            assertTrue(quarter.salesRowCount() > 0);
            assertTrue(quarter.storesRowCount() > 0);
            assertTrue(quarter.metricSnapshotCount() > 0);
        });
    }
}
