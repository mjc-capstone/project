package com.capstone.ai_insite.dataimport.domain;

public record SeoulCollectionPage(
    Long rawPayloadId,
    int rowCount,
    int totalCount,
    String responseBody
) {
}
