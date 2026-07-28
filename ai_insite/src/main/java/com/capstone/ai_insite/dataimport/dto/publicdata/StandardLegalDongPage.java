package com.capstone.ai_insite.dataimport.dto.publicdata;

import java.util.List;

public record StandardLegalDongPage(
    int totalCount,
    int pageNumber,
    int numberOfRows,
    int sourceRowCount,
    List<StandardLegalDongRow> rows
) {
}
