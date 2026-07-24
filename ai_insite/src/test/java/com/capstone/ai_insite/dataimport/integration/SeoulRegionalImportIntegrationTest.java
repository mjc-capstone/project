package com.capstone.ai_insite.dataimport.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.capstone.ai_insite.dataimport.domain.SeoulQuarter;
import com.capstone.ai_insite.dataimport.domain.SeoulRegionalImportResult;
import com.capstone.ai_insite.dataimport.service.SeoulRegionalDataImportService;
import com.capstone.ai_insite.metric.entity.RegionPeriodFeatureEntity;
import com.capstone.ai_insite.metric.repository.MetricPeriodJpaRepository;
import com.capstone.ai_insite.metric.repository.RegionPeriodFeatureJpaRepository;
import com.capstone.ai_insite.metric.repository.SourceSeoulApartmentsJpaRepository;
import com.capstone.ai_insite.metric.repository.SourceSeoulFacilitiesJpaRepository;
import com.capstone.ai_insite.metric.repository.SourceSeoulFloatingPopulationJpaRepository;
import com.capstone.ai_insite.metric.repository.SourceSeoulResidentPopulationJpaRepository;
import com.capstone.ai_insite.metric.repository.SourceSeoulWorkingPopulationJpaRepository;
import java.math.BigDecimal;
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
    named = "RUN_SEOUL_REGIONAL_API_INTEGRATION",
    matches = "true"
)
class SeoulRegionalImportIntegrationTest {

    @Autowired
    private SeoulRegionalDataImportService importService;
    @Autowired
    private MetricPeriodJpaRepository periodRepository;
    @Autowired
    private SourceSeoulFloatingPopulationJpaRepository floatingRepository;
    @Autowired
    private SourceSeoulResidentPopulationJpaRepository residentRepository;
    @Autowired
    private SourceSeoulWorkingPopulationJpaRepository workingRepository;
    @Autowired
    private SourceSeoulFacilitiesJpaRepository facilitiesRepository;
    @Autowired
    private SourceSeoulApartmentsJpaRepository apartmentsRepository;
    @Autowired
    private RegionPeriodFeatureJpaRepository featureRepository;

    @Test
    void importsActualRegionalApisAndBuildsRegionFeatures() {
        String sourcePeriod = System.getenv().getOrDefault(
            "SEOUL_TEST_PERIOD",
            "20261"
        );

        SeoulRegionalImportResult result = importService.importQuarter(sourcePeriod);

        assertEquals(SeoulQuarter.parse(sourcePeriod).periodCode(), result.periodCode());
        assertTrue(result.floatingPopulationRowCount() > 0);
        assertTrue(result.residentPopulationRowCount() > 0);
        assertTrue(result.workingPopulationRowCount() > 0);
        assertTrue(result.facilitiesRowCount() > 0);
        assertTrue(result.apartmentsRowCount() > 0);

        Long periodId = periodRepository.findByPeriodCode(result.periodCode())
            .orElseThrow()
            .getId();
        assertEquals(
            result.floatingPopulationRowCount(),
            floatingRepository.findAllByMetricPeriodId(periodId).size()
        );
        assertEquals(
            result.residentPopulationRowCount(),
            residentRepository.findAllByMetricPeriodId(periodId).size()
        );
        assertEquals(
            result.workingPopulationRowCount(),
            workingRepository.findAllByMetricPeriodId(periodId).size()
        );
        assertEquals(
            result.facilitiesRowCount(),
            facilitiesRepository.findAllByMetricPeriodId(periodId).size()
        );
        assertEquals(
            result.apartmentsRowCount(),
            apartmentsRepository.findAllByMetricPeriodId(periodId).size()
        );

        var features = featureRepository.findAllByMetricPeriodId(periodId);
        assertEquals(result.regionFeatureCount(), features.size());
        assertFalse(features.isEmpty());
        assertTrue(features.stream().allMatch(
            SeoulRegionalImportIntegrationTest::hasValidScores
        ));
    }

    private static boolean hasValidScores(RegionPeriodFeatureEntity feature) {
        return betweenZeroAndHundred(feature.getResidentialDemandScore())
            && betweenZeroAndHundred(feature.getAttractionScore())
            && betweenZeroAndHundred(feature.getTrafficAccessScore());
    }

    private static boolean betweenZeroAndHundred(BigDecimal value) {
        return value != null
            && value.compareTo(BigDecimal.ZERO) >= 0
            && value.compareTo(BigDecimal.valueOf(100)) <= 0;
    }
}
