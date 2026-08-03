package com.capstone.ai_insite.dataimport.client;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@ConditionalOnProperty(name = "external.public-data.enabled", havingValue = "true")
public class PublicDataApiHttpClient implements PublicDataApiClient {

    private final RestClient restClient;
    private final String baseUrl;
    private final String serviceKey;

    public PublicDataApiHttpClient(
        @Value("${external.public-data.base-url}") String baseUrl,
        @Value("${external.public-data.service-key}") String serviceKey
    ) {
        this.restClient = RestClient.builder()
            .requestFactory(new HttpComponentsClientHttpRequestFactory())
            .build();
        this.baseUrl = baseUrl;
        this.serviceKey = serviceKey;
    }

    @Override
    public String fetch(String path, Map<String, String> queryParameters) {
        if (serviceKey == null || serviceKey.isBlank()) {
            throw new IllegalStateException("Public data API key is required.");
        }
        byte[] responseBody = restClient.get()
            .uri(buildUri(path, queryParameters))
            .header("User-Agent", "ai-insite/1.0")
            .retrieve()
            .body(byte[].class);
        return responseBody == null
            ? ""
            : new String(responseBody, StandardCharsets.UTF_8);
    }

    URI buildUri(String path, Map<String, String> queryParameters) {
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromUriString(baseUrl)
            .path(path)
            .queryParam("serviceKey", serviceKey);
        queryParameters.forEach(builder::queryParam);
        return builder.build().encode().toUri();
    }
}
