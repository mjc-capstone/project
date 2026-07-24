package com.capstone.ai_insite.region.dto;

import com.capstone.ai_insite.region.domain.Region;
import java.math.BigDecimal;

public record RegionResponse(
    String regionCode,
    String sidoName,
    String sigunguName,
    String administrativeDongName,
    BigDecimal latitude,
    BigDecimal longitude
) {
    public static RegionResponse from(Region region) {
        return new RegionResponse(
            region.administrativeDongCode(),
            region.sidoName(),
            region.sigunguName(),
            region.administrativeDongName(),
            region.latitude(),
            region.longitude()
        );
    }
}
