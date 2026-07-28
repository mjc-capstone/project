package com.capstone.ai_insite.category.entity;

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
@Table(name = "small_business_categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SmallBusinessCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "large_category_code", nullable = false, length = 30)
    private String largeCategoryCode;

    @Column(name = "large_category_name", nullable = false, length = 100)
    private String largeCategoryName;

    @Column(name = "medium_category_code", nullable = false, length = 30)
    private String mediumCategoryCode;

    @Column(name = "medium_category_name", nullable = false, length = 100)
    private String mediumCategoryName;

    @Column(name = "small_category_code", nullable = false, length = 30)
    private String smallCategoryCode;

    @Column(name = "small_category_name", nullable = false, length = 100)
    private String smallCategoryName;

    @Column(name = "source_reference_date")
    private LocalDate sourceReferenceDate;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public static SmallBusinessCategoryEntity create(String smallCategoryCode) {
        SmallBusinessCategoryEntity entity = new SmallBusinessCategoryEntity();
        entity.smallCategoryCode = smallCategoryCode;
        return entity;
    }

    public void synchronize(
        String largeCategoryCode,
        String largeCategoryName,
        String mediumCategoryCode,
        String mediumCategoryName,
        String smallCategoryCode,
        String smallCategoryName,
        LocalDate sourceReferenceDate
    ) {
        this.largeCategoryCode = required(largeCategoryCode);
        this.largeCategoryName = required(largeCategoryName);
        this.mediumCategoryCode = required(mediumCategoryCode);
        this.mediumCategoryName = required(mediumCategoryName);
        this.smallCategoryCode = required(smallCategoryCode);
        this.smallCategoryName = required(smallCategoryName);
        this.sourceReferenceDate = sourceReferenceDate;
        this.active = true;
    }

    public void deactivate() {
        active = false;
    }

    private static String required(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Category code and name are required.");
        }
        return value.trim();
    }
}
