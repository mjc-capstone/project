package com.capstone.ai_insite.dataimport.domain;

public record RawPayloadCommand(
    String sourceName,
    String serviceName,
    String requestUrl,
    String requestParamsJson,
    String responseBodyJson,
    Integer rowCount,
    Long dataImportJobId
) {
    public RawPayloadCommand(
        String sourceName,
        String serviceName,
        String requestUrl,
        String requestParamsJson,
        String responseBodyJson,
        Integer rowCount
    ) {
        this(
            sourceName,
            serviceName,
            requestUrl,
            requestParamsJson,
            responseBodyJson,
            rowCount,
            null
        );
    }

    public RawPayloadCommand {
        if (sourceName == null || sourceName.isBlank()) {
            throw new IllegalArgumentException("sourceName은 필수입니다.");
        }
        if (serviceName == null || serviceName.isBlank()) {
            throw new IllegalArgumentException("serviceName은 필수입니다.");
        }
        if (responseBodyJson == null || responseBodyJson.isBlank()) {
            throw new IllegalArgumentException("responseBodyJson은 필수입니다.");
        }
    }
}
