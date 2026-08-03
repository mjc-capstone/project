package com.capstone.ai_insite.dataimport.dto.publicdata;

import com.capstone.ai_insite.dataimport.entity.RawApiPayloadEntity;

public record CollectedCommercialTransaction(
    RawApiPayloadEntity rawApiPayload,
    CommercialTransactionRow row
) {
}
