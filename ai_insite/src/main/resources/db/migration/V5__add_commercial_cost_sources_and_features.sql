CREATE TABLE source_reb_commercial_rent_stats (
    id BIGINT NOT NULL AUTO_INCREMENT,
    raw_api_payload_id BIGINT NOT NULL,
    metric_period_id BIGINT NOT NULL,
    statistic_table_id VARCHAR(40) NOT NULL,
    source_region_code VARCHAR(30) NOT NULL,
    source_region_name VARCHAR(100) NOT NULL,
    source_region_full_name VARCHAR(300) NOT NULL,
    region_level VARCHAR(30) NOT NULL,
    property_type VARCHAR(40) NOT NULL,
    metric_type VARCHAR(40) NOT NULL,
    metric_value DECIMAL(20, 8) NOT NULL,
    unit_name VARCHAR(50) NULL,
    source_item_code VARCHAR(30) NULL,
    source_item_name VARCHAR(100) NULL,
    source_row_json JSON NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_reb_rent_stat (
        statistic_table_id,
        metric_period_id,
        source_region_code,
        property_type,
        metric_type
    ),
    INDEX idx_reb_rent_stat_period_type (
        metric_period_id,
        property_type,
        region_level
    ),
    CONSTRAINT fk_reb_rent_stat_payload
        FOREIGN KEY (raw_api_payload_id) REFERENCES raw_api_payloads (id),
    CONSTRAINT fk_reb_rent_stat_period
        FOREIGN KEY (metric_period_id) REFERENCES metric_periods (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE source_molit_commercial_transactions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    raw_api_payload_id BIGINT NOT NULL,
    legal_dong_id BIGINT NULL,
    district_code VARCHAR(10) NOT NULL,
    district_name VARCHAR(50) NOT NULL,
    legal_dong_name VARCHAR(50) NOT NULL,
    deal_date DATE NOT NULL,
    source_signature CHAR(64) NOT NULL,
    occurrence_no INT NOT NULL,
    deal_amount_krw DECIMAL(20, 2) NOT NULL,
    building_area_sqm DECIMAL(18, 4) NULL,
    land_area_sqm DECIMAL(18, 4) NULL,
    price_per_building_area DECIMAL(20, 2) NULL,
    building_type VARCHAR(50) NULL,
    building_use VARCHAR(100) NULL,
    land_use VARCHAR(100) NULL,
    floor_no INT NULL,
    built_year INT NULL,
    lot_number_masked VARCHAR(50) NULL,
    cancelled TINYINT(1) NOT NULL DEFAULT 0,
    cancellation_day VARCHAR(20) NULL,
    dealing_type VARCHAR(50) NULL,
    source_row_json JSON NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_molit_commercial_transaction (
        district_code,
        deal_date,
        source_signature,
        occurrence_no
    ),
    INDEX idx_molit_transaction_period_dong (
        deal_date,
        legal_dong_id,
        cancelled
    ),
    CONSTRAINT fk_molit_transaction_payload
        FOREIGN KEY (raw_api_payload_id) REFERENCES raw_api_payloads (id),
    CONSTRAINT fk_molit_transaction_legal_dong
        FOREIGN KEY (legal_dong_id) REFERENCES legal_dongs (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE legal_dong_period_cost_features (
    id BIGINT NOT NULL AUTO_INCREMENT,
    legal_dong_id BIGINT NOT NULL,
    metric_period_id BIGINT NOT NULL,
    property_type VARCHAR(40) NOT NULL,
    commercial_transaction_count INT NOT NULL,
    median_commercial_price_per_area DECIMAL(20, 2) NULL,
    average_commercial_price_per_area DECIMAL(20, 2) NULL,
    price_per_area_p25 DECIMAL(20, 2) NULL,
    price_per_area_p75 DECIMAL(20, 2) NULL,
    price_growth_rate DECIMAL(10, 4) NULL,
    source_transaction_count INT NOT NULL,
    calculation_version VARCHAR(40) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_legal_dong_period_cost (
        legal_dong_id,
        metric_period_id,
        property_type
    ),
    CONSTRAINT fk_legal_dong_period_cost_dong
        FOREIGN KEY (legal_dong_id) REFERENCES legal_dongs (id),
    CONSTRAINT fk_legal_dong_period_cost_period
        FOREIGN KEY (metric_period_id) REFERENCES metric_periods (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE region_cost_features
    DROP INDEX uk_region_cost_features,
    ADD COLUMN source_system VARCHAR(40) NOT NULL DEFAULT 'LEGACY'
        AFTER metric_period_id,
    ADD COLUMN scope_key VARCHAR(150) NULL AFTER source_system,
    ADD COLUMN source_observation_count INT NOT NULL DEFAULT 0
        AFTER location_cost_score,
    ADD COLUMN rent_unit VARCHAR(50) NULL AFTER source_observation_count,
    ADD COLUMN price_unit VARCHAR(50) NULL AFTER rent_unit,
    ADD COLUMN calculation_version VARCHAR(40) NOT NULL DEFAULT 'legacy'
        AFTER price_unit;

UPDATE region_cost_features
SET scope_key = CONCAT('LEGACY:', id)
WHERE scope_key IS NULL;

ALTER TABLE region_cost_features
    MODIFY COLUMN scope_key VARCHAR(150) NOT NULL,
    ADD UNIQUE KEY uk_region_cost_feature_scope (
        source_system,
        scope_key,
        metric_period_id,
        property_type
    ),
    ADD INDEX idx_region_cost_feature_lookup (
        region_id,
        metric_period_id,
        source_system,
        property_type
    );
