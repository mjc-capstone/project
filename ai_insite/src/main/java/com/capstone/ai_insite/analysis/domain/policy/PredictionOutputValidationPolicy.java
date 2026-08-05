package com.capstone.ai_insite.analysis.domain.policy;

import com.capstone.ai_insite.analysis.domain.ModelFeatureInput;
import com.capstone.ai_insite.analysis.domain.ModelPrediction;
import com.capstone.ai_insite.analysis.domain.PredictionEnvelope;
import org.springframework.stereotype.Component;

@Component
public class PredictionOutputValidationPolicy {

    public PredictionEnvelope validate(
        ModelFeatureInput input,
        PredictionEnvelope output
    ) {
        if (!input.requestId().equals(output.requestId())) {
            throw new IllegalArgumentException("모델 응답 requestId가 일치하지 않습니다.");
        }
        if (!input.requestedModelReleaseVersion().equals(
            output.modelReleaseVersion()
        )) {
            throw new IllegalArgumentException("모델 릴리스 버전이 일치하지 않습니다.");
        }
        ModelPrediction prediction = output.prediction();
        requireFinite(prediction.nextQuarterSalesGrowthRate(), "매출 변화 추정치");
        requireProbability(prediction.storeDeclineProbability(), "점포 감소 지수");
        requireFinite(prediction.nextQuarterCloseRate(), "다음 분기 폐업률");
        requireFinite(prediction.fourQuarterStoreRetentionRate(), "점포 유지율");
        requireProbability(
            prediction.storeBaseMaintainedProbability(),
            "점포 기반 유지 지수"
        );
        requireProbability(output.quality().missingFeatureRate(), "핵심 피처 결측률");
        if (output.inferenceMillis() < 0) {
            throw new IllegalArgumentException("모델 추론 시간은 음수일 수 없습니다.");
        }
        return output;
    }

    private static void requireFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + "가 유한한 수가 아닙니다.");
        }
    }

    private static void requireProbability(double value, String name) {
        requireFinite(value, name);
        if (value < 0 || value > 1) {
            throw new IllegalArgumentException(name + "가 0~1 범위를 벗어났습니다.");
        }
    }
}
