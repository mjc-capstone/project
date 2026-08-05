package com.capstone.ai_insite.analysis.domain.policy;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.capstone.ai_insite.analysis.domain.MarketFeatureVector;
import com.capstone.ai_insite.analysis.domain.ModelFeatureInput;
import com.capstone.ai_insite.analysis.domain.ModelPrediction;
import com.capstone.ai_insite.analysis.domain.PredictionEnvelope;
import com.capstone.ai_insite.analysis.domain.PredictionQuality;
import com.capstone.ai_insite.analysis.domain.PredictionSource;
import com.capstone.ai_insite.analysis.domain.UserConditionVector;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class PredictionOutputValidationPolicyTest {

    private final PredictionOutputValidationPolicy policy =
        new PredictionOutputValidationPolicy();

    @Test
    void acceptsFiniteMatchingModelOutput() {
        var input = input();
        var output = output(0.2, input.requestId(), input.requestedModelReleaseVersion());

        assertSame(output, policy.validate(input, output));
    }

    @Test
    void rejectsMismatchedReleaseAndInvalidProbability() {
        var input = input();

        assertThrows(
            IllegalArgumentException.class,
            () -> policy.validate(input, output(0.2, input.requestId(), "other"))
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> policy.validate(
                input,
                output(1.2, input.requestId(), input.requestedModelReleaseVersion())
            )
        );
    }

    private static ModelFeatureInput input() {
        return new ModelFeatureInput(
            "request-1",
            "core-v1",
            "feature-v3-building",
            1L,
            LocalDate.of(2026, 3, 31),
            new MarketFeatureVector(
                "CS100001", "11110515", 1.0, 1.0, null, 1.0,
                null, 50.0, 50.0, 50.0, 50.0, 10.0,
                1000.0, 500.0, 300.0
            ),
            UserConditionVector.empty()
        );
    }

    private static PredictionEnvelope output(
        double probability,
        String requestId,
        String release
    ) {
        return new PredictionEnvelope(
            requestId,
            release,
            new ModelPrediction(1.0, probability, 2.0, 100.0, 0.5),
            new PredictionQuality(true, 0.0, List.of()),
            PredictionSource.REMOTE_MODEL,
            false,
            null,
            1
        );
    }
}
