package com.capstone.ai_insite.dataimport.dto.publicdata;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BuildingRegisterRow(
    String buildingRegisterId,
    String districtCode,
    String legalDongSourceCode,
    String lotAddress,
    String roadAddress,
    String buildingName,
    String dongName,
    String registerKindCode,
    String registerKindName,
    String mainAttachmentCode,
    String mainAttachmentName,
    String mainUseCode,
    String mainUseName,
    String otherUse,
    BigDecimal siteAreaSquareMeter,
    BigDecimal buildingAreaSquareMeter,
    BigDecimal grossFloorAreaSquareMeter,
    BigDecimal buildingCoverageRatio,
    BigDecimal floorAreaRatio,
    Integer groundFloorCount,
    Integer basementFloorCount,
    LocalDate approvalDate,
    int parkingCount,
    int elevatorCount,
    LocalDate sourceCreatedDate,
    String sourceRowJson
) {
}
