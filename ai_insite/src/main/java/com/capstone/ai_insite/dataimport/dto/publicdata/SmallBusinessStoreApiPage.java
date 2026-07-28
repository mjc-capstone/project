package com.capstone.ai_insite.dataimport.dto.publicdata;

import java.util.List;

public record SmallBusinessStoreApiPage(
    String standardMonth,
    int pageNo,
    int numberOfRows,
    int totalCount,
    List<SmallBusinessStoreApiRow> rows
) {
}
