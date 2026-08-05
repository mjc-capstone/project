package com.capstone.ai_insite.dataimport.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.capstone.ai_insite.dataimport.domain.SeoulQuarter;
import com.capstone.ai_insite.dataimport.service.CostHistoryDataImportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "external.public-data.enabled=true",
    "external.reb.enabled=true",
    "spring.jpa.show-sql=false"
})
@EnabledIfEnvironmentVariable(
    named = "RUN_P3_COST_HISTORY_INTEGRATION",
    matches = "true"
)
class P3CostHistoryImportIntegrationTest {

    @Autowired
    private CostHistoryDataImportService importService;

    @Test
    void importsCostSourcesForTheConfiguredRange() {
        String from = System.getenv().getOrDefault("P3_COST_FROM", "20232");
        String to = System.getenv().getOrDefault("P3_COST_TO", "20254");

        var result = importService.importRange(from, to, "p6-backfill");

        assertEquals(SeoulQuarter.rangeInclusive(from, to, 20).size(), result.quarterCount());
        assertTrue(result.totalRebRows() > 0);
        assertTrue(result.totalTransactionRows() > 0);
        assertTrue(result.totalGeneratedFeatures() > 0);
        result.quarters().forEach(quarter -> {
            assertTrue(quarter.transactions().generatedFeatureCount() > 0);
        });
    }
}
