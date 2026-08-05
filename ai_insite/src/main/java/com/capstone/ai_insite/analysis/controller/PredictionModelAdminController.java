package com.capstone.ai_insite.analysis.controller;

import com.capstone.ai_insite.analysis.dto.ModelPredictionResponse;
import com.capstone.ai_insite.analysis.service.PredictionExecutionApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/prediction-models")
@ConditionalOnProperty(name = "prediction.model.enabled", havingValue = "true")
public class PredictionModelAdminController {

    private final PredictionExecutionApplicationService predictionService;

    public PredictionModelAdminController(
        PredictionExecutionApplicationService predictionService
    ) {
        this.predictionService = predictionService;
    }

    @GetMapping("/candidate-smoke")
    public ModelPredictionResponse smoke(@RequestParam Long featureSnapshotId) {
        return ModelPredictionResponse.from(
            predictionService.predict(featureSnapshotId)
        );
    }
}
