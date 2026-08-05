package com.capstone.ai_insite.analysis.domain.port;

import com.capstone.ai_insite.analysis.domain.ModelFeatureInput;
import com.capstone.ai_insite.analysis.domain.PredictionEnvelope;

public interface AnalysisPredictionPort {

    PredictionEnvelope predict(ModelFeatureInput input);
}
