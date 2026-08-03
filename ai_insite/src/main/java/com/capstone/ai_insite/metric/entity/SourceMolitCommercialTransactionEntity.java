package com.capstone.ai_insite.metric.entity;

import com.capstone.ai_insite.dataimport.dto.publicdata.CommercialTransactionRow;
import com.capstone.ai_insite.dataimport.entity.RawApiPayloadEntity;
import com.capstone.ai_insite.region.entity.LegalDongEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "source_molit_commercial_transactions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SourceMolitCommercialTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "raw_api_payload_id", nullable = false)
    private RawApiPayloadEntity rawApiPayload;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "legal_dong_id")
    private LegalDongEntity legalDong;

    @Column(name = "district_code", nullable = false, length = 10)
    private String districtCode;

    @Column(name = "district_name", nullable = false, length = 50)
    private String districtName;

    @Column(name = "legal_dong_name", nullable = false, length = 50)
    private String legalDongName;

    @Column(name = "deal_date", nullable = false)
    private LocalDate dealDate;

    @Column(name = "source_signature", nullable = false, length = 64)
    private String sourceSignature;

    @Column(name = "occurrence_no", nullable = false)
    private int occurrenceNo;

    @Column(name = "deal_amount_krw", nullable = false, precision = 20, scale = 2)
    private BigDecimal dealAmountKrw;

    @Column(name = "building_area_sqm", precision = 18, scale = 4)
    private BigDecimal buildingAreaSquareMeter;

    @Column(name = "land_area_sqm", precision = 18, scale = 4)
    private BigDecimal landAreaSquareMeter;

    @Column(name = "price_per_building_area", precision = 20, scale = 2)
    private BigDecimal pricePerBuildingArea;

    @Column(name = "building_type", length = 50)
    private String buildingType;

    @Column(name = "building_use", length = 100)
    private String buildingUse;

    @Column(name = "land_use", length = 100)
    private String landUse;

    @Column(name = "floor_no")
    private Integer floor;

    @Column(name = "built_year")
    private Integer builtYear;

    @Column(name = "lot_number_masked", length = 50)
    private String lotNumberMasked;

    @Column(name = "cancelled", nullable = false)
    private boolean cancelled;

    @Column(name = "cancellation_day", length = 20)
    private String cancellationDay;

    @Column(name = "dealing_type", length = 50)
    private String dealingType;

    @Column(name = "source_row_json", nullable = false, columnDefinition = "json")
    private String sourceRowJson;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public SourceMolitCommercialTransactionEntity(
        RawApiPayloadEntity rawApiPayload,
        LegalDongEntity legalDong,
        CommercialTransactionRow row,
        String sourceSignature,
        int occurrenceNo
    ) {
        this.rawApiPayload = rawApiPayload;
        this.legalDong = legalDong;
        this.districtCode = row.districtCode();
        this.districtName = row.districtName();
        this.legalDongName = row.legalDongName();
        this.dealDate = row.dealDate();
        this.sourceSignature = sourceSignature;
        this.occurrenceNo = occurrenceNo;
        this.dealAmountKrw = row.dealAmountKrw();
        this.buildingAreaSquareMeter = row.buildingAreaSquareMeter();
        this.landAreaSquareMeter = row.landAreaSquareMeter();
        this.pricePerBuildingArea = pricePerArea(
            row.dealAmountKrw(),
            row.buildingAreaSquareMeter()
        );
        this.buildingType = row.buildingType();
        this.buildingUse = row.buildingUse();
        this.landUse = row.landUse();
        this.floor = row.floor();
        this.builtYear = row.builtYear();
        this.lotNumberMasked = row.lotNumberMasked();
        this.cancelled = row.cancelled();
        this.cancellationDay = row.cancellationDay();
        this.dealingType = row.dealingType();
        this.sourceRowJson = row.sourceRowJson();
    }

    private static BigDecimal pricePerArea(
        BigDecimal amount,
        BigDecimal area
    ) {
        if (area == null || area.signum() <= 0) {
            return null;
        }
        return amount.divide(area, 2, RoundingMode.HALF_UP);
    }
}
