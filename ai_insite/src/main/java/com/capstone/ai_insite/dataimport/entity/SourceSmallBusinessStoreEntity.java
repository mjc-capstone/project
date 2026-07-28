package com.capstone.ai_insite.dataimport.entity;

import com.capstone.ai_insite.category.entity.BusinessCategoryEntity;
import com.capstone.ai_insite.dataimport.domain.SmallBusinessStoreImportCommand;
import com.capstone.ai_insite.region.entity.LegalDongEntity;
import com.capstone.ai_insite.region.entity.RegionEntity;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "source_small_business_stores")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SourceSmallBusinessStoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raw_api_payload_id")
    private RawApiPayloadEntity rawApiPayload;

    @Column(name = "external_store_id", nullable = false, length = 50)
    private String externalStoreId;
    @Column(name = "store_name", length = 200)
    private String storeName;
    @Column(name = "source_large_category_code", length = 30)
    private String sourceLargeCategoryCode;
    @Column(name = "source_large_category_name", length = 100)
    private String sourceLargeCategoryName;
    @Column(name = "source_medium_category_code", length = 30)
    private String sourceMediumCategoryCode;
    @Column(name = "source_medium_category_name", length = 100)
    private String sourceMediumCategoryName;
    @Column(name = "source_small_category_code", length = 30)
    private String sourceSmallCategoryCode;
    @Column(name = "source_small_category_name", length = 100)
    private String sourceSmallCategoryName;
    @Column(name = "ksic_code", length = 30)
    private String ksicCode;
    @Column(name = "ksic_name", length = 100)
    private String ksicName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private RegionEntity region;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "legal_dong_id")
    private LegalDongEntity legalDong;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_category_id")
    private BusinessCategoryEntity businessCategory;

    @Column(name = "administrative_dong_code", length = 20)
    private String administrativeDongCode;
    @Column(name = "legal_dong_code", length = 20)
    private String legalDongCode;
    @Column(name = "region_mapping_confidence", precision = 5, scale = 4)
    private BigDecimal regionMappingConfidence;
    @Column(name = "region_mapping_rule", length = 100)
    private String regionMappingRule;
    @Column(name = "category_mapping_confidence", precision = 5, scale = 4)
    private BigDecimal categoryMappingConfidence;
    @Column(name = "category_mapping_rule", length = 100)
    private String categoryMappingRule;
    @Column(name = "jibun_address", length = 300)
    private String jibunAddress;
    @Column(name = "road_address", length = 300)
    private String roadAddress;
    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;
    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;
    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;
    @Column(name = "source_updated_at")
    private LocalDateTime sourceUpdatedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "source_row_json", columnDefinition = "json")
    private String sourceRowJson;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public SourceSmallBusinessStoreEntity(
        String externalStoreId,
        LocalDate snapshotDate
    ) {
        this.externalStoreId = externalStoreId;
        this.snapshotDate = snapshotDate;
    }

    public void apply(
        SmallBusinessStoreImportCommand command,
        RawApiPayloadEntity rawPayload,
        RegionEntity region,
        LegalDongEntity legalDong,
        BusinessCategoryEntity category,
        BigDecimal regionConfidence,
        String regionRule,
        BigDecimal categoryConfidence,
        String categoryRule
    ) {
        rawApiPayload = rawPayload;
        externalStoreId = command.externalStoreId();
        storeName = command.storeName();
        sourceLargeCategoryCode = command.sourceLargeCategoryCode();
        sourceLargeCategoryName = command.sourceLargeCategoryName();
        sourceMediumCategoryCode = command.sourceMediumCategoryCode();
        sourceMediumCategoryName = command.sourceMediumCategoryName();
        sourceSmallCategoryCode = command.sourceSmallCategoryCode();
        sourceSmallCategoryName = command.sourceSmallCategoryName();
        ksicCode = command.ksicCode();
        ksicName = command.ksicName();
        this.region = region;
        this.legalDong = legalDong;
        businessCategory = category;
        administrativeDongCode = command.administrativeDongCode();
        legalDongCode = command.legalDongCode();
        regionMappingConfidence = regionConfidence;
        regionMappingRule = regionRule;
        categoryMappingConfidence = categoryConfidence;
        categoryMappingRule = categoryRule;
        jibunAddress = command.jibunAddress();
        roadAddress = command.roadAddress();
        longitude = command.longitude();
        latitude = command.latitude();
        snapshotDate = command.snapshotDate();
        sourceUpdatedAt = command.sourceUpdatedAt();
        sourceRowJson = command.sourceRowJson();
    }
}
