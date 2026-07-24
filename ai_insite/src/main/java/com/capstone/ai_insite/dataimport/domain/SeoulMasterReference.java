package com.capstone.ai_insite.dataimport.domain;

public record SeoulMasterReference(
    String sourcePeriodCode,
    String regionCode,
    String regionName,
    String categoryCode,
    String categoryName
) implements SeoulRegionPeriodReference {
    public SeoulMasterReference {
        if (regionCode == null || regionCode.isBlank()) {
            throw new IllegalArgumentException("서울시 행정동 코드는 필수입니다.");
        }
        if (categoryCode == null || categoryCode.isBlank()) {
            throw new IllegalArgumentException("서울시 업종 코드는 필수입니다.");
        }
        SeoulQuarter.parse(sourcePeriodCode);
    }
}
