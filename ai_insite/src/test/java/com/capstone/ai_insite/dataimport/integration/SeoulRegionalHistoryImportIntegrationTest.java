package com.capstone.ai_insite.dataimport.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.capstone.ai_insite.dataimport.domain.SeoulQuarter;
import com.capstone.ai_insite.dataimport.service.SeoulRegionalDataImportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "external.seoul.enabled=true",
    "external.seoul.scheduler-enabled=false",
    "external.seoul.page-size=1000",
    "external.seoul.max-pages=100",
    "spring.jpa.show-sql=false"
})
@EnabledIfEnvironmentVariable(
    named = "RUN_SEOUL_REGIONAL_HISTORY_INTEGRATION",
    matches = "true"
)
class SeoulRegionalHistoryImportIntegrationTest {

    @Autowired
    private SeoulRegionalDataImportService importService;

    @Test
    void importsEveryRegionalSourceForTheConfiguredRange() {
        String from = System.getenv().getOrDefault("SEOUL_REGIONAL_FROM", "20232");
        String to = System.getenv().getOrDefault("SEOUL_REGIONAL_TO", "20254");

        var result = importService.importRange(from, to);

        assertEquals(SeoulQuarter.rangeInclusive(from, to, 20).size(), result.quarterCount());
        assertTrue(result.totalFloatingPopulationRows() > 0);
        assertTrue(result.totalResidentPopulationRows() > 0);
        assertTrue(result.totalWorkingPopulationRows() > 0);
        assertTrue(result.totalFacilitiesRows() > 0);
        assertTrue(result.totalApartmentsRows() > 0);
        result.quarters().forEach(quarter -> {
            assertTrue(quarter.regionFeatureCount() > 0);
            assertTrue(quarter.metricSnapshotCount() > 0);
        });
    }
}
