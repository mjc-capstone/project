package com.capstone.ai_insite.analysis.client;

import com.capstone.ai_insite.analysis.domain.ModelFeatureInput;
import com.capstone.ai_insite.analysis.domain.ModelPrediction;
import com.capstone.ai_insite.analysis.domain.PredictionEnvelope;
import com.capstone.ai_insite.analysis.domain.PredictionQuality;
import com.capstone.ai_insite.analysis.domain.PredictionSource;
import com.capstone.ai_insite.analysis.domain.UserConditionVector;
import com.capstone.ai_insite.analysis.domain.port.AnalysisPredictionPort;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "prediction.model.enabled", havingValue = "true")
public class RemoteModelPredictionAdapter implements AnalysisPredictionPort {

    private final RestClient restClient;

    public RemoteModelPredictionAdapter(PredictionModelProperties properties) {
        SimpleClientHttpRequestFactory requestFactory =
            new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getReadTimeout());
        this.restClient = RestClient.builder()
            .baseUrl(properties.getBaseUrl())
            .requestFactory(requestFactory)
            .build();
    }

    @Override
    public PredictionEnvelope predict(ModelFeatureInput input) {
        RemotePredictionResponse response = restClient.post()
            .uri("/v1/predictions")
            .body(RemotePredictionRequest.from(input))
            .retrieve()
            .body(RemotePredictionResponse.class);
        if (response == null) {
            throw new IllegalStateException("모델 서버 응답이 비어 있습니다.");
        }
        return response.toDomain();
    }

    private record RemotePredictionRequest(
        String requestId,
        String schemaVersion,
        String requestedModelReleaseVersion,
        String featureVersion,
        Long featureSnapshotId,
        java.time.LocalDate featureAsOfDate,
        com.capstone.ai_insite.analysis.domain.MarketFeatureVector marketFeatures,
        UserConditionVector userCondition
    ) {

        private static RemotePredictionRequest from(ModelFeatureInput input) {
            return new RemotePredictionRequest(
                input.requestId(),
                "prediction-input-v1",
                input.requestedModelReleaseVersion(),
                input.featureVersion(),
                input.featureSnapshotId(),
                input.featureAsOfDate(),
                input.marketFeatures(),
                input.userCondition()
            );
        }
    }

    private record RemotePredictionResponse(
        String requestId,
        String schemaVersion,
        String modelReleaseVersion,
        RemotePredictions predictions,
        RemoteQuality quality,
        long inferenceMillis
    ) {

        private PredictionEnvelope toDomain() {
            return new PredictionEnvelope(
                requestId,
                modelReleaseVersion,
                predictions.toDomain(),
                quality.toDomain(),
                PredictionSource.REMOTE_MODEL,
                false,
                null,
                inferenceMillis
            );
        }
    }

    private record RemotePredictions(
        double nextQuarterSalesGrowthRate,
        double storeDeclineProbability,
        double nextQuarterCloseRate,
        double fourQuarterStoreRetentionRate,
        double storeBaseMaintainedProbability
    ) {

        private ModelPrediction toDomain() {
            return new ModelPrediction(
                nextQuarterSalesGrowthRate,
                storeDeclineProbability,
                nextQuarterCloseRate,
                fourQuarterStoreRetentionRate,
                storeBaseMaintainedProbability
            );
        }
    }

    private record RemoteQuality(
        boolean inDistribution,
        double missingFeatureRate,
        List<String> warnings
    ) {

        private PredictionQuality toDomain() {
            return new PredictionQuality(
                inDistribution,
                missingFeatureRate,
                warnings
            );
        }
    }
}
