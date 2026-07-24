package com.capstone.ai_insite.dataimport.client;

import java.util.Map;

public interface PublicDataApiClient {

    String fetch(String path, Map<String, String> queryParameters);
}
