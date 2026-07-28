package com.capstone.ai_insite.category.entity;

import com.capstone.ai_insite.category.domain.MappingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "category_mapping_candidates")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryMappingCandidateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "small_business_category_id", nullable = false)
    private SmallBusinessCategoryEntity smallBusinessCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposed_business_category_id")
    private BusinessCategoryEntity proposedBusinessCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "mapping_status", nullable = false, length = 30)
    private MappingStatus mappingStatus;

    @Column(name = "mapping_confidence", precision = 5, scale = 4)
    private BigDecimal mappingConfidence;

    @Column(name = "mapping_rule", length = 100)
    private String mappingRule;

    @Column(name = "evidence_count", nullable = false)
    private long evidenceCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "observed_ksic_codes_json", columnDefinition = "json")
    private String observedKsicCodesJson;

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

    public static CategoryMappingCandidateEntity create(
        SmallBusinessCategoryEntity source
    ) {
        CategoryMappingCandidateEntity entity = new CategoryMappingCandidateEntity();
        entity.smallBusinessCategory = source;
        entity.mappingStatus = MappingStatus.UNRESOLVED;
        return entity;
    }

    public void propose(
        BusinessCategoryEntity target,
        MappingStatus status,
        BigDecimal confidence,
        String rule,
        long evidenceCount,
        String observedKsicCodesJson
    ) {
        if (mappingStatus == MappingStatus.CONFIRMED
            || mappingStatus == MappingStatus.REJECTED) {
            return;
        }
        this.proposedBusinessCategory = target;
        this.mappingStatus = status;
        this.mappingConfidence = confidence;
        this.mappingRule = rule;
        this.evidenceCount = evidenceCount;
        this.observedKsicCodesJson = observedKsicCodesJson;
    }

    public void confirm(
        BusinessCategoryEntity target,
        String reviewedBy,
        String reviewNote
    ) {
        this.proposedBusinessCategory = target;
        this.mappingStatus = MappingStatus.CONFIRMED;
        this.mappingConfidence = BigDecimal.ONE.setScale(4);
        this.mappingRule = "MANUAL_REVIEW";
        this.reviewedBy = required(reviewedBy);
        this.reviewedAt = LocalDateTime.now();
        this.reviewNote = blankToNull(reviewNote);
    }

    public void reject(String reviewedBy, String reviewNote) {
        this.mappingStatus = MappingStatus.REJECTED;
        this.reviewedBy = required(reviewedBy);
        this.reviewedAt = LocalDateTime.now();
        this.reviewNote = blankToNull(reviewNote);
    }

    private static String required(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Reviewer is required.");
        }
        return value.trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
