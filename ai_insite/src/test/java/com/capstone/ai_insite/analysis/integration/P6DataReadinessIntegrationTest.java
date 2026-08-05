package com.capstone.ai_insite.analysis.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.capstone.ai_insite.analysis.domain.ModelDatasetAuditStatus;
import com.capstone.ai_insite.analysis.domain.ModelDatasetBuildCommand;
import com.capstone.ai_insite.analysis.repository.ModelDatasetBuildJpaRepository;
import com.capstone.ai_insite.analysis.service.ModelDatasetApplicationService;
import com.capstone.ai_insite.analysis.service.ModelDatasetAuditService;
import com.capstone.ai_insite.analysis.service.ModelFeatureLabelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "external.seoul.enabled=false",
    "external.seoul.scheduler-enabled=false"
})
@EnabledIfEnvironmentVariable(named = "RUN_P6_DATA_READINESS", matches = "true")
class P6DataReadinessIntegrationTest {

    private static final String DATASET_VERSION =
        "seoul-commercial-2023q2-2026q1-v1";

    @Autowired
    private ModelFeatureLabelService featureLabelService;

    @Autowired
    private ModelDatasetApplicationService datasetService;

    @Autowired
    private ModelDatasetAuditService auditService;

    @Autowired
    private ModelDatasetBuildJpaRepository datasetRepository;

    @Test
    void buildsAndAuditsTheHistoricalSeoulTrainingDataset() {
        var existing = datasetRepository.findByDatasetVersion(DATASET_VERSION);
        var dataset = existing
            .map(entity -> datasetService.get(entity.getId()))
            .orElseGet(() -> {
                var labels = featureLabelService.rebuild("2023Q2", "2026Q1");
                assertTrue(labels.processedCount() >= 150_000);
                assertTrue(labels.readyCount() >= 100_000);
                return datasetService.build(new ModelDatasetBuildCommand(
                DATASET_VERSION,
                "2023Q2",
                "2024Q4",
                "2025Q2",
                "2026Q1"
                ));
            });

        assertTrue(dataset.trainExampleCount() > 0);
        assertTrue(dataset.validationExampleCount() > 0);
        assertTrue(dataset.testExampleCount() > 0);
        assertEquals(
            ModelDatasetAuditStatus.READY,
            auditService.audit(dataset.id()).status()
        );
    }
}
