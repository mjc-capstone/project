package com.capstone.ai_insite.dataimport.client;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "external.reb.enabled", havingValue = "true")
public class RebApiHttpClient implements RebApiClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String statisticsPath;

    public RebApiHttpClient(
        @Value("${external.reb.base-url}") String baseUrl,
        @Value("${external.reb.api-key}") String apiKey,
        @Value("${external.reb.statistics-path}") String statisticsPath
    ) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.statisticsPath = statisticsPath;
    }

    @Override
    public String fetchStatistics(Map<String, String> queryParameters) {
        return restClient.get()
            .uri(uriBuilder -> {
                var builder = uriBuilder.path(statisticsPath)
                    .queryParam("KEY", apiKey);
                queryParameters.forEach(builder::queryParam);
                return builder.build();
            })
            .retrieve()
            .body(String.class);
    }
}
