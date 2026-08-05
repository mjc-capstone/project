package com.capstone.ai_insite.analysis.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.capstone.ai_insite.analysis.domain.ModelLabelStatus;
import com.capstone.ai_insite.analysis.repository.ModelFeatureSnapshotJpaRepository;
import com.capstone.ai_insite.analysis.service.FeatureBuildService;
import com.capstone.ai_insite.analysis.service.PredictionExecutionApplicationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "prediction.model.enabled=true",
    "prediction.model.base-url=http://127.0.0.1:8000",
    "prediction.model.release-version=core-v1-2026-08",
    "spring.jpa.show-sql=false"
})
@EnabledIfEnvironmentVariable(named = "RUN_MODEL_SERVER_E2E", matches = "true")
class RemoteModelPredictionE2eIntegrationTest {

    @Autowired
    private PredictionExecutionApplicationService predictionService;

    @Autowired
    private ModelFeatureSnapshotJpaRepository featureRepository;

    @Test
    void predictsThroughTheRealFastApiContainer() {
        var feature = featureRepository
            .findFirstByFeatureVersionAndLabelStatusOrderByIdAsc(
                FeatureBuildService.FEATURE_VERSION,
                ModelLabelStatus.READY
            )
            .orElseThrow();

        var result = predictionService.predict(feature.getId());

        assertEquals("core-v1-2026-08", result.modelReleaseVersion());
        assertEquals("REMOTE_MODEL", result.source().name());
        assertFalse(result.fallbackUsed());
    }
}
