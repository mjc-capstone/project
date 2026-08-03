package com.capstone.ai_insite.dataimport.dto.publicdata;

import java.util.List;

public record CommercialTransactionPage(
    int pageNumber,
    int pageSize,
    int totalCount,
    List<CommercialTransactionRow> rows
) {
}
