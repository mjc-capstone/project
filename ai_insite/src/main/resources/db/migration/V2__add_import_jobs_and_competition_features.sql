CREATE TABLE data_import_jobs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source_name VARCHAR(100) NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    target_period VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    started_at DATETIME(6) NULL,
    completed_at DATETIME(6) NULL,
    requested_by VARCHAR(100) NULL,
    total_page_count INT NOT NULL DEFAULT 0,
    fetched_row_count BIGINT NOT NULL DEFAULT 0,
    normalized_row_count BIGINT NOT NULL DEFAULT 0,
    rejected_row_count BIGINT NOT NULL DEFAULT 0,
    error_message TEXT NULL,
    retry_of_job_id BIGINT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    active_marker TINYINT GENERATED ALWAYS AS (
        CASE WHEN status IN ('PENDING', 'RUNNING') THEN 1 ELSE NULL END
    ) STORED,
    PRIMARY KEY (id),
    UNIQUE KEY uk_data_import_jobs_active (
        source_name,
        service_name,
        target_period,
        active_marker
    ),
    INDEX idx_data_import_jobs_status_created (status, created_at),
    CONSTRAINT fk_data_import_jobs_retry
        FOREIGN KEY (retry_of_job_id) REFERENCES data_import_jobs (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE raw_api_payloads
    ADD COLUMN data_import_job_id BIGINT NULL AFTER id,
    ADD INDEX idx_raw_api_payloads_job (data_import_job_id),
    ADD CONSTRAINT fk_raw_api_payloads_job
        FOREIGN KEY (data_import_job_id) REFERENCES data_import_jobs (id);

ALTER TABLE source_small_business_stores
    ADD COLUMN raw_api_payload_id BIGINT NULL AFTER id,
    ADD COLUMN business_category_id BIGINT NULL AFTER legal_dong_id,
    ADD COLUMN region_mapping_confidence DECIMAL(5, 4) NULL AFTER legal_dong_code,
    ADD COLUMN region_mapping_rule VARCHAR(100) NULL AFTER region_mapping_confidence,
    ADD COLUMN category_mapping_confidence DECIMAL(5, 4) NULL AFTER region_mapping_rule,
    ADD COLUMN category_mapping_rule VARCHAR(100) NULL AFTER category_mapping_confidence,
    ADD INDEX idx_small_business_stores_mapped (
        region_id,
        business_category_id,
        snapshot_date
    ),
    ADD CONSTRAINT fk_small_business_stores_raw
        FOREIGN KEY (raw_api_payload_id) REFERENCES raw_api_payloads (id),
    ADD CONSTRAINT fk_small_business_stores_category
        FOREIGN KEY (business_category_id) REFERENCES business_categories (id);

CREATE TABLE commercial_competition_features (
    id BIGINT NOT NULL AUTO_INCREMENT,
    region_id BIGINT NOT NULL,
    business_category_id BIGINT NOT NULL,
    metric_period_id BIGINT NOT NULL,
    snapshot_date DATE NOT NULL,
    active_store_count INT NOT NULL,
    store_count_per_square_km DECIMAL(12, 4) NULL,
    same_category_store_count INT NOT NULL,
    franchise_store_count INT NULL,
    category_diversity_index DECIMAL(12, 6) NULL,
    source_store_snapshot_count INT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_commercial_competition_features (
        region_id,
        business_category_id,
        metric_period_id,
        snapshot_date
    ),
    INDEX idx_competition_features_lookup (
        region_id,
        business_category_id,
        metric_period_id
    ),
    CONSTRAINT fk_competition_features_region
        FOREIGN KEY (region_id) REFERENCES regions (id),
    CONSTRAINT fk_competition_features_category
        FOREIGN KEY (business_category_id) REFERENCES business_categories (id),
    CONSTRAINT fk_competition_features_period
        FOREIGN KEY (metric_period_id) REFERENCES metric_periods (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
