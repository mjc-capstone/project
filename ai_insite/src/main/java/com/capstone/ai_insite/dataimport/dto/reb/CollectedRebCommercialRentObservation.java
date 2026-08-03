package com.capstone.ai_insite.dataimport.dto.reb;

import com.capstone.ai_insite.dataimport.entity.RawApiPayloadEntity;

public record CollectedRebCommercialRentObservation(
    RawApiPayloadEntity rawApiPayload,
    RebCommercialRentObservation observation
) {
}
