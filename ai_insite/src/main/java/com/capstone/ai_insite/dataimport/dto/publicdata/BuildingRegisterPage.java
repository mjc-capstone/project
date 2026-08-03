package com.capstone.ai_insite.dataimport.dto.publicdata;

import java.util.List;

public record BuildingRegisterPage(
    int pageNumber,
    int numberOfRows,
    int totalCount,
    List<BuildingRegisterRow> rows
) {
}
