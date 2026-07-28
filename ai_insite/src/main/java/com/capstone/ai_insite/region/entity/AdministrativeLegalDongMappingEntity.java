package com.capstone.ai_insite.region.entity;

import com.capstone.ai_insite.region.domain.RegionMappingStatus;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "administrative_legal_dong_mappings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdministrativeLegalDongMappingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private RegionEntity region;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "legal_dong_id", nullable = false)
    private LegalDongEntity legalDong;

    @Column(name = "mapping_confidence", nullable = false, precision = 5, scale = 4)
    private BigDecimal mappingConfidence;

    @Column(name = "mapping_rule", nullable = false, length = 100)
    private String mappingRule;

    @Enumerated(EnumType.STRING)
    @Column(name = "mapping_status", nullable = false, length = 30)
    private RegionMappingStatus mappingStatus;

    @Column(name = "evidence_count", nullable = false)
    private long evidenceCount;

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_note", length = 500)
    private String reviewNote;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public static AdministrativeLegalDongMappingEntity create(
        RegionEntity region,
        LegalDongEntity legalDong
    ) {
        AdministrativeLegalDongMappingEntity entity =
            new AdministrativeLegalDongMappingEntity();
        entity.region = region;
        entity.legalDong = legalDong;
        return entity;
    }

    public void synchronizeObserved(long evidenceCount, LocalDate observedAt) {
        this.evidenceCount = evidenceCount;
        this.mappingRule = "OBSERVED_STORE_CODE_PAIR";
        if (mappingStatus == RegionMappingStatus.CONFIRMED
            || mappingStatus == RegionMappingStatus.REJECTED) {
            return;
        }
        this.mappingConfidence = evidenceCount >= 3
            ? new BigDecimal("0.9500")
            : new BigDecimal("0.7000");
        this.mappingStatus = evidenceCount >= 3
            ? RegionMappingStatus.AUTO_CONFIRMED
            : RegionMappingStatus.CANDIDATE;
        if (effectiveFrom == null || observedAt.isBefore(effectiveFrom)) {
            effectiveFrom = observedAt;
        }
        effectiveTo = null;
    }

    public void confirm(String reviewer, String note) {
        if (reviewer == null || reviewer.isBlank()) {
            throw new IllegalArgumentException("Reviewer is required.");
        }
        this.mappingStatus = RegionMappingStatus.CONFIRMED;
        this.mappingConfidence = BigDecimal.ONE.setScale(4);
        this.reviewedBy = reviewer.trim();
        this.reviewedAt = LocalDateTime.now();
        this.reviewNote = note;
    }

    public void reject(String reviewer, String note) {
        if (reviewer == null || reviewer.isBlank()) {
            throw new IllegalArgumentException("Reviewer is required.");
        }
        this.mappingStatus = RegionMappingStatus.REJECTED;
        this.reviewedBy = reviewer.trim();
        this.reviewedAt = LocalDateTime.now();
        this.reviewNote = note;
    }

    public boolean isUsable() {
        return mappingStatus != null && mappingStatus.isUsable();
    }
}
