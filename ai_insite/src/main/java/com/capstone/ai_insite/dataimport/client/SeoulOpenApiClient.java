package com.capstone.ai_insite.dataimport.client;

public interface SeoulOpenApiClient {

    String fetch(
        String serviceName,
        int startIndex,
        int endIndex,
        String sourcePeriodCode
    );
}
