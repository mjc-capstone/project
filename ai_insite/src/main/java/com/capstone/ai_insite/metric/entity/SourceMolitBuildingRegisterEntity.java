package com.capstone.ai_insite.metric.entity;

import com.capstone.ai_insite.dataimport.dto.publicdata.BuildingRegisterRow;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "source_molit_building_registers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SourceMolitBuildingRegisterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "raw_api_payload_id", nullable = false)
    private RawApiPayloadEntity rawApiPayload;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "legal_dong_id", nullable = false)
    private LegalDongEntity legalDong;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "building_register_id", nullable = false, length = 50)
    private String buildingRegisterId;

    @Column(name = "district_code", nullable = false, length = 10)
    private String districtCode;

    @Column(name = "legal_dong_code_snapshot", nullable = false, length = 20)
    private String legalDongCodeSnapshot;

    @Column(name = "lot_address", length = 300)
    private String lotAddress;

    @Column(name = "road_address", length = 300)
    private String roadAddress;

    @Column(name = "building_name", length = 200)
    private String buildingName;

    @Column(name = "dong_name", length = 100)
    private String dongName;

    @Column(name = "register_kind_code", length = 20)
    private String registerKindCode;

    @Column(name = "register_kind_name", length = 50)
    private String registerKindName;

    @Column(name = "main_attachment_code", length = 20)
    private String mainAttachmentCode;

    @Column(name = "main_attachment_name", length = 50)
    private String mainAttachmentName;

    @Column(name = "main_use_code", length = 20)
    private String mainUseCode;

    @Column(name = "main_use_name", length = 100)
    private String mainUseName;

    @Column(name = "other_use", length = 300)
    private String otherUse;

    @Column(name = "site_area_sqm", precision = 20, scale = 4)
    private BigDecimal siteAreaSquareMeter;

    @Column(name = "building_area_sqm", precision = 20, scale = 4)
    private BigDecimal buildingAreaSquareMeter;

    @Column(name = "gross_floor_area_sqm", precision = 20, scale = 4)
    private BigDecimal grossFloorAreaSquareMeter;

    @Column(name = "building_coverage_ratio", precision = 12, scale = 4)
    private BigDecimal buildingCoverageRatio;

    @Column(name = "floor_area_ratio", precision = 12, scale = 4)
    private BigDecimal floorAreaRatio;

    @Column(name = "ground_floor_count")
    private Integer groundFloorCount;

    @Column(name = "basement_floor_count")
    private Integer basementFloorCount;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Column(name = "parking_count", nullable = false)
    private int parkingCount;

    @Column(name = "elevator_count", nullable = false)
    private int elevatorCount;

    @Column(name = "source_created_date")
    private LocalDate sourceCreatedDate;

    @Column(name = "source_row_json", nullable = false, columnDefinition = "json")
    private String sourceRowJson;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public SourceMolitBuildingRegisterEntity(
        RawApiPayloadEntity rawApiPayload,
        LegalDongEntity legalDong,
        LocalDate snapshotDate,
        BuildingRegisterRow row
    ) {
        this.rawApiPayload = rawApiPayload;
        this.legalDong = legalDong;
        this.snapshotDate = snapshotDate;
        this.buildingRegisterId = row.buildingRegisterId();
        this.districtCode = row.districtCode();
        this.legalDongCodeSnapshot = legalDong.getLegalDongCode();
        this.lotAddress = row.lotAddress();
        this.roadAddress = row.roadAddress();
        this.buildingName = row.buildingName();
        this.dongName = row.dongName();
        this.registerKindCode = row.registerKindCode();
        this.registerKindName = row.registerKindName();
        this.mainAttachmentCode = row.mainAttachmentCode();
        this.mainAttachmentName = row.mainAttachmentName();
        this.mainUseCode = row.mainUseCode();
        this.mainUseName = row.mainUseName();
        this.otherUse = row.otherUse();
        this.siteAreaSquareMeter = row.siteAreaSquareMeter();
        this.buildingAreaSquareMeter = row.buildingAreaSquareMeter();
        this.grossFloorAreaSquareMeter = row.grossFloorAreaSquareMeter();
        this.buildingCoverageRatio = row.buildingCoverageRatio();
        this.floorAreaRatio = row.floorAreaRatio();
        this.groundFloorCount = row.groundFloorCount();
        this.basementFloorCount = row.basementFloorCount();
        this.approvalDate = row.approvalDate();
        this.parkingCount = row.parkingCount();
        this.elevatorCount = row.elevatorCount();
        this.sourceCreatedDate = row.sourceCreatedDate();
        this.sourceRowJson = row.sourceRowJson();
    }
}
