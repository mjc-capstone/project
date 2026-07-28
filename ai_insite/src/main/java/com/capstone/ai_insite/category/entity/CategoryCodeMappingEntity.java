package com.capstone.ai_insite.category.entity;

import com.capstone.ai_insite.category.domain.MappingReviewType;
import com.capstone.ai_insite.category.domain.MappingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "category_code_mappings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryCodeMappingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seoul_service_category_code", length = 30)
    private String seoulServiceCategoryCode;

    @Column(name = "small_business_category_code", length = 30)
    private String smallBusinessCategoryCode;

    @Column(name = "ksic_code", length = 30)
    private String ksicCode;

    @Column(name = "normalized_category_code", nullable = false, length = 30)
    private String normalizedCategoryCode;

    @Column(name = "normalized_category_name", length = 100)
    private String normalizedCategoryName;

    @Column(name = "mapping_confidence", nullable = false, precision = 5, scale = 4)
    private BigDecimal mappingConfidence;

    @Column(name = "mapping_rule", nullable = false, length = 100)
    private String mappingRule;

    @Enumerated(EnumType.STRING)
    @Column(name = "mapping_status", nullable = false, length = 30)
    private MappingStatus mappingStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false, length = 30)
    private MappingReviewType reviewType;

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_note", length = 500)
    private String reviewNote;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public static CategoryCodeMappingEntity create(
        String smallBusinessCategoryCode,
        String ksicCode,
        BusinessCategoryEntity target,
        BigDecimal confidence,
        String rule,
        MappingStatus status,
        MappingReviewType reviewType,
        String reviewedBy,
        String reviewNote
    ) {
        CategoryCodeMappingEntity entity = new CategoryCodeMappingEntity();
        entity.smallBusinessCategoryCode = blankToNull(smallBusinessCategoryCode);
        entity.ksicCode = blankToNull(ksicCode);
        entity.normalizedCategoryCode = target.getNormalizedCategoryCode();
        entity.normalizedCategoryName = target.getNormalizedCategoryName();
        entity.mappingConfidence = confidence;
        entity.mappingRule = rule;
        entity.mappingStatus = status;
        entity.reviewType = reviewType;
        entity.reviewedBy = blankToNull(reviewedBy);
        entity.reviewedAt = reviewedBy == null ? null : LocalDateTime.now();
        entity.reviewNote = blankToNull(reviewNote);
        return entity;
    }

    public boolean isConfirmed() {
        return mappingStatus != null && mappingStatus.isConfirmed();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
