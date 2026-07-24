package com.capstone.ai_insite.dataimport.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "external.kakao.enabled", havingValue = "true")
public class KakaoLocalApiHttpClient implements KakaoLocalApiClient {

    private final RestClient restClient;
    private final String apiKey;

    public KakaoLocalApiHttpClient(
        @Value("${external.kakao.base-url}") String baseUrl,
        @Value("${external.kakao.api-key}") String apiKey
    ) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    @Override
    public String searchKeyword(String keyword, int page, int size) {
        return restClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v2/local/search/keyword.json")
                .queryParam("query", keyword)
                .queryParam("page", page)
                .queryParam("size", size)
                .build())
            .header("Authorization", "KakaoAK " + apiKey)
            .retrieve()
            .body(String.class);
    }
}
