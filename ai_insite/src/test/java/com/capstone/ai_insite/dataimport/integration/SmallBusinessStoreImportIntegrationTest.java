package com.capstone.ai_insite.dataimport.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.capstone.ai_insite.dataimport.domain.DataImportJobStatus;
import com.capstone.ai_insite.dataimport.repository.DataImportJobJpaRepository;
import com.capstone.ai_insite.dataimport.repository.RawApiPayloadJpaRepository;
import com.capstone.ai_insite.dataimport.repository.SourceSmallBusinessStoreJpaRepository;
import com.capstone.ai_insite.dataimport.service.SmallBusinessStoreDataImportService;
import com.capstone.ai_insite.metric.repository.CommercialCompetitionFeatureJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "external.public-data.enabled=true",
    "external.public-data.small-business.page-size=1000",
    "external.public-data.small-business.max-pages-per-district=100"
})
@EnabledIfEnvironmentVariable(
    named = "RUN_SMALL_BUSINESS_API_INTEGRATION",
    matches = "true"
)
class SmallBusinessStoreImportIntegrationTest {

    @Autowired
    private SmallBusinessStoreDataImportService importService;
    @Autowired
    private DataImportJobJpaRepository jobRepository;
    @Autowired
    private RawApiPayloadJpaRepository rawPayloadRepository;
    @Autowired
    private SourceSmallBusinessStoreJpaRepository storeRepository;
    @Autowired
    private CommercialCompetitionFeatureJpaRepository featureRepository;

    @Test
    void importsActualJongnoStorePagesAndBuildsCompetitionFeatures() {
        var result = importService.collect("11110", "live-integration-test");
        var job = jobRepository.findById(result.jobId()).orElseThrow();

        assertEquals(DataImportJobStatus.COMPLETED, job.getStatus());
        assertEquals(result.pageCount(), job.getTotalPageCount());
        assertEquals(result.fetchedRowCount(), job.getFetchedRowCount());
        assertEquals(result.normalizedRowCount(), job.getNormalizedRowCount());
        assertEquals(
            result.pageCount(),
            rawPayloadRepository.countByDataImportJobId(result.jobId())
        );
        assertTrue(result.fetchedRowCount() > 0);
        assertTrue(storeRepository.countBySnapshotDate(result.snapshotDate()) > 0);
        assertEquals(
            result.competitionFeatureCount(),
            featureRepository.findBySnapshotDate(result.snapshotDate()).size()
        );
    }
}
