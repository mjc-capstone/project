package com.capstone.ai_insite.dataimport.client;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "external.public-data.enabled", havingValue = "true")
public class PublicDataApiHttpClient implements PublicDataApiClient {

    private final RestClient restClient;
    private final String serviceKey;

    public PublicDataApiHttpClient(
        @Value("${external.public-data.base-url}") String baseUrl,
        @Value("${external.public-data.service-key}") String serviceKey
    ) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.serviceKey = serviceKey;
    }

    @Override
    public String fetch(String path, Map<String, String> queryParameters) {
        return restClient.get()
            .uri(uriBuilder -> {
                var builder = uriBuilder.path(path).queryParam("serviceKey", serviceKey);
                queryParameters.forEach(builder::queryParam);
                return builder.build();
            })
            .retrieve()
            .body(String.class);
    }
}
