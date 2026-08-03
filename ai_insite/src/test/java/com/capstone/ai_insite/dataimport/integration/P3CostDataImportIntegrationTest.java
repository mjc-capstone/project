package com.capstone.ai_insite.dataimport.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.capstone.ai_insite.dataimport.service.CommercialTransactionDataImportService;
import com.capstone.ai_insite.dataimport.service.RebCommercialRentDataImportService;
import com.capstone.ai_insite.metric.repository.LegalDongPeriodCostFeatureJpaRepository;
import com.capstone.ai_insite.metric.repository.RegionCostFeatureJpaRepository;
import com.capstone.ai_insite.metric.repository.SourceMolitCommercialTransactionJpaRepository;
import com.capstone.ai_insite.metric.repository.SourceRebCommercialRentStatJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "external.public-data.enabled=true",
    "external.reb.enabled=true"
})
@EnabledIfEnvironmentVariable(
    named = "RUN_P3_COST_API_INTEGRATION",
    matches = "true"
)
class P3CostDataImportIntegrationTest {

    @Autowired
    private RebCommercialRentDataImportService rebImportService;
    @Autowired
    private CommercialTransactionDataImportService transactionImportService;
    @Autowired
    private SourceRebCommercialRentStatJpaRepository rebSourceRepository;
    @Autowired
    private SourceMolitCommercialTransactionJpaRepository transactionRepository;
    @Autowired
    private RegionCostFeatureJpaRepository regionCostRepository;
    @Autowired
    private LegalDongPeriodCostFeatureJpaRepository legalCostRepository;

    @Test
    void importsP3SourcesAndBuildsCostFeatures() {
        var reb = rebImportService.importQuarter("20261", "p3-live-test", null);
        var transactions = transactionImportService.importQuarter(
            "20261",
            "p3-live-test",
            null
        );

        assertTrue(reb.normalizedRowCount() > 0);
        assertTrue(transactions.normalizedRowCount() > 0);
        assertTrue(rebSourceRepository.count() > 0);
        assertTrue(transactionRepository.count() > 0);
        assertTrue(regionCostRepository.count() > 0);
        assertTrue(legalCostRepository.count() > 0);
    }
}
