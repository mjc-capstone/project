package com.capstone.ai_insite.analysis.domain;

import java.util.List;

public record PredictionQuality(
    boolean inDistribution,
    double missingFeatureRate,
    List<String> warnings
) {

    public PredictionQuality {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
