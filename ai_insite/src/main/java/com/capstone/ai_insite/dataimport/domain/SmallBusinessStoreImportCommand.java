package com.capstone.ai_insite.dataimport.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SmallBusinessStoreImportCommand(
    String externalStoreId,
    String storeName,
    String sourceLargeCategoryCode,
    String sourceLargeCategoryName,
    String sourceMediumCategoryCode,
    String sourceMediumCategoryName,
    String sourceSmallCategoryCode,
    String sourceSmallCategoryName,
    String ksicCode,
    String ksicName,
    String administrativeDongCode,
    String legalDongCode,
    String jibunAddress,
    String roadAddress,
    BigDecimal longitude,
    BigDecimal latitude,
    LocalDate snapshotDate,
    LocalDateTime sourceUpdatedAt,
    String sourceRowJson
) {
    public SmallBusinessStoreImportCommand {
        if (externalStoreId == null || externalStoreId.isBlank()) {
            throw new IllegalArgumentException("상가업소번호는 필수입니다.");
        }
        if (snapshotDate == null) {
            throw new IllegalArgumentException("상가정보 스냅샷 날짜는 필수입니다.");
        }
        if (sourceRowJson == null || sourceRowJson.isBlank()) {
            throw new IllegalArgumentException("상가정보 원본 행 JSON은 필수입니다.");
        }
    }
}
