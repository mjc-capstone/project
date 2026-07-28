ALTER TABLE regions
    ADD COLUMN source_system VARCHAR(50) NULL AFTER is_active,
    ADD COLUMN effective_from DATE NULL AFTER source_system,
    ADD COLUMN effective_to DATE NULL AFTER effective_from;

ALTER TABLE legal_dongs
    ADD COLUMN source_system VARCHAR(50) NULL AFTER is_active,
    ADD COLUMN source_reference_date DATE NULL AFTER source_system;

ALTER TABLE administrative_legal_dong_mappings
    ADD COLUMN mapping_status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED'
        AFTER mapping_rule,
    ADD COLUMN evidence_count BIGINT NOT NULL DEFAULT 0 AFTER mapping_status,
    ADD COLUMN reviewed_by VARCHAR(100) NULL AFTER evidence_count,
    ADD COLUMN reviewed_at DATETIME(6) NULL AFTER reviewed_by,
    ADD COLUMN review_note VARCHAR(500) NULL AFTER reviewed_at,
    ADD INDEX idx_admin_legal_mapping_status (
        mapping_status,
        effective_from,
        effective_to
    );

ALTER TABLE category_code_mappings
    ADD COLUMN mapping_status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED'
        AFTER mapping_rule,
    ADD COLUMN review_type VARCHAR(30) NOT NULL DEFAULT 'AUTO'
        AFTER mapping_status,
    ADD COLUMN reviewed_by VARCHAR(100) NULL AFTER review_type,
    ADD COLUMN reviewed_at DATETIME(6) NULL AFTER reviewed_by,
    ADD COLUMN review_note VARCHAR(500) NULL AFTER reviewed_at,
    ADD INDEX idx_category_mapping_status (mapping_status, review_type);

CREATE TABLE small_business_categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    large_category_code VARCHAR(30) NOT NULL,
    large_category_name VARCHAR(100) NOT NULL,
    medium_category_code VARCHAR(30) NOT NULL,
    medium_category_name VARCHAR(100) NOT NULL,
    small_category_code VARCHAR(30) NOT NULL,
    small_category_name VARCHAR(100) NOT NULL,
    source_reference_date DATE NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_small_business_categories_code (small_category_code),
    INDEX idx_small_business_categories_hierarchy (
        large_category_code,
        medium_category_code
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE category_mapping_candidates (
    id BIGINT NOT NULL AUTO_INCREMENT,
    small_business_category_id BIGINT NOT NULL,
    proposed_business_category_id BIGINT NULL,
    mapping_status VARCHAR(30) NOT NULL,
    mapping_confidence DECIMAL(5, 4) NULL,
    mapping_rule VARCHAR(100) NULL,
    evidence_count BIGINT NOT NULL DEFAULT 0,
    observed_ksic_codes_json JSON NULL,
    reviewed_by VARCHAR(100) NULL,
    reviewed_at DATETIME(6) NULL,
    review_note VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_mapping_candidate_source (
        small_business_category_id
    ),
    INDEX idx_category_mapping_candidate_status (
        mapping_status,
        mapping_confidence
    ),
    CONSTRAINT fk_category_candidate_small_business
        FOREIGN KEY (small_business_category_id)
        REFERENCES small_business_categories (id),
    CONSTRAINT fk_category_candidate_proposed
        FOREIGN KEY (proposed_business_category_id)
        REFERENCES business_categories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
