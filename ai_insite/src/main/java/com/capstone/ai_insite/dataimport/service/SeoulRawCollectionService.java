package com.capstone.ai_insite.dataimport.service;

import com.capstone.ai_insite.dataimport.client.SeoulOpenApiClient;
import com.capstone.ai_insite.dataimport.domain.RawPayloadCommand;
import com.capstone.ai_insite.dataimport.domain.SeoulCollectionPage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@ConditionalOnProperty(name = "external.seoul.enabled", havingValue = "true")
public class SeoulRawCollectionService {

    private final SeoulOpenApiClient apiClient;
    private final RawPayloadService rawPayloadService;
    private final ObjectMapper objectMapper;

    public SeoulRawCollectionService(
        SeoulOpenApiClient apiClient,
        RawPayloadService rawPayloadService,
        ObjectMapper objectMapper
    ) {
        this.apiClient = apiClient;
        this.rawPayloadService = rawPayloadService;
        this.objectMapper = objectMapper;
    }

    public SeoulCollectionPage collect(
        String serviceName,
        int startIndex,
        int endIndex,
        String sourcePeriodCode
    ) {
        return collectInternal(
            serviceName,
            startIndex,
            endIndex,
            sourcePeriodCode,
            sourcePeriodCode
        );
    }

    public SeoulCollectionPage collectHistory(
        String serviceName,
        int startIndex,
        int endIndex,
        String targetSourcePeriodCode
    ) {
        return collectInternal(
            serviceName,
            startIndex,
            endIndex,
            targetSourcePeriodCode,
            null
        );
    }

    private SeoulCollectionPage collectInternal(
        String serviceName,
        int startIndex,
        int endIndex,
        String targetSourcePeriodCode,
        String apiPeriodFilter
    ) {
        String response = apiClient.fetch(
            serviceName,
            startIndex,
            endIndex,
            apiPeriodFilter
        );
        JsonNode servicePayload = servicePayload(response, serviceName);
        int rowCount = servicePayload.path("row").size();
        int totalCount = servicePayload.path("list_total_count").asInt(rowCount);
        Long rawPayloadId = rawPayloadService.save(new RawPayloadCommand(
            "SEOUL_OPEN_DATA",
            serviceName,
            "seoul-open-api://" + serviceName,
            "{\"startIndex\":" + startIndex
                + ",\"endIndex\":" + endIndex
                + ",\"targetSourcePeriodCode\":\"" + targetSourcePeriodCode
                + "\",\"apiPeriodFilter\":"
                + (apiPeriodFilter == null ? "null" : "\"" + apiPeriodFilter + "\"")
                + "}",
            response,
            rowCount
        )).getId();
        return new SeoulCollectionPage(rawPayloadId, rowCount, totalCount, response);
    }

    private JsonNode servicePayload(String response, String serviceName) {
        try {
            JsonNode payload = objectMapper.readTree(response).path(serviceName);
            if (payload.isMissingNode()) {
                throw new IllegalStateException(
                    "서울 API 응답에 서비스 노드가 없습니다: " + serviceName
                );
            }
            return payload;
        } catch (Exception exception) {
            throw new IllegalStateException("서울 API 응답 해석에 실패했습니다.", exception);
        }
    }
}
