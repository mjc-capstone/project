package com.capstone.ai_insite.dataimport.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.capstone.ai_insite.dataimport.domain.SeoulCommercialImportResult;
import com.capstone.ai_insite.dataimport.domain.SeoulQuarter;
import com.capstone.ai_insite.dataimport.service.SeoulCommercialDataImportService;
import com.capstone.ai_insite.metric.repository.CommercialMetricSnapshotJpaRepository;
import com.capstone.ai_insite.metric.repository.MetricPeriodJpaRepository;
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
    named = "RUN_SEOUL_API_INTEGRATION",
    matches = "true"
)
class SeoulCommercialImportIntegrationTest {

    @Autowired
    private SeoulCommercialDataImportService importService;

    @Autowired
    private CommercialMetricSnapshotJpaRepository snapshotRepository;

    @Autowired
    private MetricPeriodJpaRepository periodRepository;

    @Test
    void importsConfiguredQuarterAndBuildsMetricSnapshots() {
        String sourcePeriod = System.getenv().getOrDefault("SEOUL_TEST_PERIOD", "20261");

        SeoulCommercialImportResult result = importService.importQuarter(sourcePeriod);

        assertEquals(SeoulQuarter.parse(sourcePeriod).periodCode(), result.periodCode());
        assertTrue(result.salesRowCount() > 0);
        assertTrue(result.storesRowCount() > 0);
        assertTrue(result.metricSnapshotCount() > 0);
        Long periodId = periodRepository.findByPeriodCode(result.periodCode())
            .orElseThrow()
            .getId();
        assertEquals(
            result.metricSnapshotCount(),
            snapshotRepository.findAllByMetricPeriodId(periodId).size()
        );
    }
}
