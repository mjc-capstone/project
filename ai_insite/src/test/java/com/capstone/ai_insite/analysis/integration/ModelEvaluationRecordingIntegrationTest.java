package com.capstone.ai_insite.analysis.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.capstone.ai_insite.analysis.domain.ModelEvaluationCommand;
import com.capstone.ai_insite.analysis.repository.ModelDatasetBuildJpaRepository;
import com.capstone.ai_insite.analysis.service.ModelDatasetApplicationService;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "external.seoul.enabled=false",
    "external.seoul.scheduler-enabled=false"
})
@EnabledIfEnvironmentVariable(
    named = "RUN_MODEL_EVALUATION_RECORDING",
    matches = "true"
)
class ModelEvaluationRecordingIntegrationTest {

    @Autowired
    private ModelDatasetApplicationService datasetService;
    @Autowired
    private ModelDatasetBuildJpaRepository datasetRepository;

    @Test
    void recordsCoreAndEnrichedMetricsAgainstTheirDatasets() throws Exception {
        Path artifactRoot = Path.of(required("MODEL_ARTIFACT_ROOT"));

        record(
            "seoul-commercial-2023q2-2026q1-v1",
            "core-v1-2026-08",
            artifactRoot.resolve("core-v1-2026-08/metrics.json")
        );
        record(
            "seoul-commercial-enriched-2023q2-2026q1-v1",
            "enriched-v1-2026-08",
            artifactRoot.resolve("enriched-v1-2026-08/metrics.json")
        );
    }

    private void record(
        String datasetVersion,
        String modelVersion,
        Path metricsPath
    ) throws Exception {
        var dataset = datasetRepository.findByDatasetVersion(datasetVersion)
            .orElseThrow();
        var result = datasetService.recordEvaluation(
            dataset.getId(),
            new ModelEvaluationCommand(
                modelVersion,
                Files.readString(metricsPath)
            )
        );

        assertEquals(modelVersion, result.modelVersion());
        assertNotNull(result.evaluationMetricsJson());
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value;
    }
}
