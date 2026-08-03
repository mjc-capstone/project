package com.capstone.ai_insite.dataimport.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.capstone.ai_insite.dataimport.service.BuildingRegisterDataImportService;
import com.capstone.ai_insite.metric.repository.BuildingRegionMappingJpaRepository;
import com.capstone.ai_insite.metric.repository.RegionBuiltEnvironmentFeatureJpaRepository;
import com.capstone.ai_insite.metric.repository.SourceMolitBuildingRegisterJpaRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "external.public-data.enabled=true")
@EnabledIfEnvironmentVariable(
    named = "RUN_P4_BUILDING_API_INTEGRATION",
    matches = "true"
)
class P4BuildingRegisterDataImportIntegrationTest {

    @Autowired
    private BuildingRegisterDataImportService importService;
    @Autowired
    private SourceMolitBuildingRegisterJpaRepository sourceRepository;
    @Autowired
    private BuildingRegionMappingJpaRepository mappingRepository;
    @Autowired
    private RegionBuiltEnvironmentFeatureJpaRepository featureRepository;

    @Test
    void importsRealBuildingHubResponseAndBuildsFeatures() {
        LocalDate snapshotDate = LocalDate.now();
        int quarter = (snapshotDate.getMonthValue() - 1) / 3 + 1;
        String sourcePeriod = snapshotDate.getYear() + String.valueOf(quarter);
        var result = importService.importSnapshot(
            sourcePeriod,
            "11110",
            "1111010100",
            "p4-live-test",
            null
        );

        assertTrue(result.normalizedRowCount() > 0);
        assertTrue(result.requestedPageCount() > 1);
        assertTrue(result.normalizedRowCount() == result.fetchedRowCount());
        assertTrue(sourceRepository.findBySnapshotDate(snapshotDate).stream()
            .anyMatch(source -> source.getLotAddress() != null
                && source.getLotAddress().contains("청운동")));
        assertTrue(mappingRepository
            .findBySourceBuildingSnapshotDate(snapshotDate).size() > 0);
        assertTrue(featureRepository.count() > 0);
    }
}
