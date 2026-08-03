package com.capstone.ai_insite.dataimport.dto.reb;

import java.util.List;

public record RebCommercialRentPage(
    int totalCount,
    int sourceRowCount,
    List<RebCommercialRentObservation> observations
) {
}
