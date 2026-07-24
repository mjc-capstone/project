package com.capstone.ai_insite.dataimport.dto;

import com.capstone.ai_insite.dataimport.domain.RawPayloadCommand;
import tools.jackson.databind.JsonNode;

public record RawPayloadRequest(
    String sourceName,
    String serviceName,
    String requestUrl,
    JsonNode requestParams,
    JsonNode responseBody,
    Integer rowCount
) {
    public RawPayloadCommand toCommand() {
        return new RawPayloadCommand(
            sourceName,
            serviceName,
            requestUrl,
            requestParams == null ? null : requestParams.toString(),
            responseBody == null ? null : responseBody.toString(),
            rowCount
        );
    }
}
