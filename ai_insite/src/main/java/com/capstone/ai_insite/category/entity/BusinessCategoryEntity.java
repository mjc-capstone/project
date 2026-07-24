package com.capstone.ai_insite.category.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "business_categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BusinessCategoryEntity {

    public static final String SEOUL_COMMERCIAL_SOURCE = "SEOUL_COMMERCIAL";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_system", nullable = false, length = 50)
    private String sourceSystem;

    @Column(name = "source_category_code", nullable = false, length = 30)
    private String sourceCategoryCode;

    @Column(name = "source_category_name", nullable = false, length = 100)
    private String sourceCategoryName;

    @Column(name = "large_category_code", length = 30)
    private String largeCategoryCode;

    @Column(name = "large_category_name", length = 100)
    private String largeCategoryName;

    @Column(name = "medium_category_code", length = 30)
    private String mediumCategoryCode;

    @Column(name = "medium_category_name", length = 100)
    private String mediumCategoryName;

    @Column(name = "small_category_code", length = 30)
    private String smallCategoryCode;

    @Column(name = "small_category_name", length = 100)
    private String smallCategoryName;

    @Column(name = "normalized_category_code", length = 30)
    private String normalizedCategoryCode;

    @Column(name = "normalized_category_name", length = 100)
    private String normalizedCategoryName;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public static BusinessCategoryEntity createSeoulCommercial(
        String sourceCategoryCode,
        String sourceCategoryName
    ) {
        BusinessCategoryEntity entity = new BusinessCategoryEntity();
        entity.sourceSystem = SEOUL_COMMERCIAL_SOURCE;
        entity.synchronizeSeoulCommercial(sourceCategoryCode, sourceCategoryName);
        return entity;
    }

    public void synchronizeSeoulCommercial(
        String sourceCategoryCode,
        String sourceCategoryName
    ) {
        this.sourceSystem = SEOUL_COMMERCIAL_SOURCE;
        this.sourceCategoryCode = sourceCategoryCode;
        this.sourceCategoryName = sourceCategoryName;
        this.normalizedCategoryCode = sourceCategoryCode;
        this.normalizedCategoryName = sourceCategoryName;
        this.active = true;
    }
}
