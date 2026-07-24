package com.capstone.ai_insite.category.entity;

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

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
