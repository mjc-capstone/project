package com.capstone.ai_insite.analysis.service;

import com.capstone.ai_insite.analysis.domain.PredictionEnvelope;
import com.capstone.ai_insite.analysis.domain.port.AnalysisPredictionPort;
import com.capstone.ai_insite.analysis.domain.policy.PredictionOutputValidationPolicy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "prediction.model.enabled", havingValue = "true")
public class PredictionExecutionApplicationService {

    private final ModelFeatureInputFactory inputFactory;
    private final AnalysisPredictionPort predictionPort;
    private final PredictionOutputValidationPolicy validationPolicy;

    public PredictionExecutionApplicationService(
        ModelFeatureInputFactory inputFactory,
        AnalysisPredictionPort predictionPort,
        PredictionOutputValidationPolicy validationPolicy
    ) {
        this.inputFactory = inputFactory;
        this.predictionPort = predictionPort;
        this.validationPolicy = validationPolicy;
    }

    public PredictionEnvelope predict(Long featureSnapshotId) {
        var input = inputFactory.create(featureSnapshotId);
        return validationPolicy.validate(input, predictionPort.predict(input));
    }
}
