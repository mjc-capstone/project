package com.capstone.ai_insite.dataimport.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "external.seoul.enabled", havingValue = "true")
public class SeoulOpenApiHttpClient implements SeoulOpenApiClient {

    private final RestClient restClient;
    private final String apiKey;

    public SeoulOpenApiHttpClient(
        @Value("${external.seoul.base-url}") String baseUrl,
        @Value("${external.seoul.api-key}") String apiKey
    ) {
        SimpleClientHttpRequestFactory requestFactory =
            new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(java.time.Duration.ofSeconds(10));
        requestFactory.setReadTimeout(java.time.Duration.ofSeconds(60));
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(requestFactory)
            .build();
        this.apiKey = apiKey;
    }

    @Override
    public String fetch(
        String serviceName,
        int startIndex,
        int endIndex,
        String sourcePeriodCode
    ) {
        if (startIndex < 1 || endIndex < startIndex) {
            throw new IllegalArgumentException("서울 API 조회 범위가 올바르지 않습니다.");
        }
        if (sourcePeriodCode != null && !sourcePeriodCode.matches("\\d{5}")) {
            throw new IllegalArgumentException("서울 API 기준 년분기 코드가 올바르지 않습니다.");
        }
        if (sourcePeriodCode == null) {
            return restClient.get()
                .uri("/{key}/json/{service}/{start}/{end}",
                    apiKey, serviceName, startIndex, endIndex)
                .retrieve()
                .body(String.class);
        }
        return restClient.get()
            .uri("/{key}/json/{service}/{start}/{end}/{period}",
                apiKey, serviceName, startIndex, endIndex, sourcePeriodCode)
            .retrieve()
            .body(String.class);
    }
}
