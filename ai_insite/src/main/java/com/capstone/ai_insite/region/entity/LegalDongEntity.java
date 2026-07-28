package com.capstone.ai_insite.region.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "legal_dongs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LegalDongEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "legal_dong_code", nullable = false, length = 20)
    private String legalDongCode;

    @Column(name = "sido_code", length = 20)
    private String sidoCode;

    @Column(name = "sido_name", nullable = false, length = 50)
    private String sidoName;

    @Column(name = "sigungu_code", length = 20)
    private String sigunguCode;

    @Column(name = "sigungu_name", nullable = false, length = 50)
    private String sigunguName;

    @Column(name = "legal_dong_name", nullable = false, length = 50)
    private String legalDongName;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "source_system", length = 50)
    private String sourceSystem;

    @Column(name = "source_reference_date")
    private LocalDate sourceReferenceDate;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public static LegalDongEntity create(String legalDongCode) {
        LegalDongEntity entity = new LegalDongEntity();
        entity.legalDongCode = legalDongCode;
        return entity;
    }

    public void synchronize(
        String legalDongCode,
        String sidoCode,
        String sidoName,
        String sigunguCode,
        String sigunguName,
        String legalDongName,
        LocalDate effectiveFrom,
        LocalDate sourceReferenceDate
    ) {
        this.legalDongCode = legalDongCode;
        this.sidoCode = sidoCode;
        this.sidoName = sidoName;
        this.sigunguCode = sigunguCode;
        this.sigunguName = sigunguName;
        this.legalDongName = legalDongName;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = null;
        this.active = true;
        this.sourceSystem = "MOIS_STANDARD_REGION_CODE";
        this.sourceReferenceDate = sourceReferenceDate;
    }

    public void deactivate(LocalDate effectiveTo) {
        this.active = false;
        this.effectiveTo = effectiveTo;
    }
}
