package com.capstone.ai_insite.region.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "regions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "administrative_dong_code", nullable = false, length = 20)
    private String administrativeDongCode;

    @Column(name = "sido_code", length = 20)
    private String sidoCode;

    @Column(name = "sido_name", nullable = false, length = 50)
    private String sidoName;

    @Column(name = "sigungu_code", length = 20)
    private String sigunguCode;

    @Column(name = "sigungu_name", nullable = false, length = 50)
    private String sigunguName;

    @Column(name = "administrative_dong_name", nullable = false, length = 50)
    private String administrativeDongName;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public static RegionEntity createSeoulAdministrativeDong(
        String administrativeDongCode,
        String administrativeDongName,
        String sigunguCode,
        String sigunguName
    ) {
        RegionEntity entity = new RegionEntity();
        entity.sidoCode = "11";
        entity.sidoName = "서울특별시";
        entity.synchronizeSeoulAdministrativeDong(
            administrativeDongCode,
            administrativeDongName,
            sigunguCode,
            sigunguName
        );
        return entity;
    }

    public void synchronizeSeoulAdministrativeDong(
        String administrativeDongCode,
        String administrativeDongName,
        String sigunguCode,
        String sigunguName
    ) {
        this.administrativeDongCode = administrativeDongCode;
        this.administrativeDongName = administrativeDongName;
        this.sigunguCode = sigunguCode;
        this.sigunguName = sigunguName;
        this.sidoCode = "11";
        this.sidoName = "서울특별시";
        this.active = true;
    }
}
