package com.capstone.ai_insite.analysis.entity;

import com.capstone.ai_insite.analysis.domain.AnalysisCommand;
import com.capstone.ai_insite.category.entity.BusinessCategoryEntity;
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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "analysis_requests")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private RegionEntity region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_category_id")
    private BusinessCategoryEntity businessCategory;

    @Column(name = "input_address", length = 300)
    private String inputAddress;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "user_budget")
    private Long userBudget;

    @Column(name = "user_max_rent")
    private Long userMaxRent;

    @Column(name = "target_monthly_sales")
    private Long targetMonthlySales;

    @Column(name = "preferred_area_square_meter", precision = 12, scale = 2)
    private BigDecimal preferredAreaSquareMeter;

    @Column(name = "operation_type", length = 50)
    private String operationType;

    @Column(name = "franchise_yn")
    private Boolean franchise;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "target_customer_json", columnDefinition = "json")
    private String targetCustomerJson;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public AnalysisRequestEntity(
        RegionEntity region,
        BusinessCategoryEntity category,
        AnalysisCommand command
    ) {
        this.region = region;
        this.businessCategory = category;
        this.inputAddress = command.inputAddress();
        this.latitude = command.latitude();
        this.longitude = command.longitude();
        this.userBudget = command.condition().budget();
        this.userMaxRent = command.condition().maxMonthlyRent();
        this.targetMonthlySales = command.condition().targetMonthlySales();
        this.preferredAreaSquareMeter = command.condition().preferredAreaSquareMeter();
        this.operationType = command.condition().operationType();
        this.franchise = command.condition().franchise();
        this.targetCustomerJson = command.condition().targetCustomerJson();
    }
}
