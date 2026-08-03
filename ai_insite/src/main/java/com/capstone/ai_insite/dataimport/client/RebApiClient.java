package com.capstone.ai_insite.dataimport.client;

import java.util.Map;

public interface RebApiClient {

    String fetchStatistics(Map<String, String> queryParameters);
}
