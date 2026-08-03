CREATE TABLE source_molit_building_registers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    raw_api_payload_id BIGINT NOT NULL,
    legal_dong_id BIGINT NOT NULL,
    snapshot_date DATE NOT NULL,
    building_register_id VARCHAR(50) NOT NULL,
    district_code VARCHAR(10) NOT NULL,
    legal_dong_code_snapshot VARCHAR(20) NOT NULL,
    lot_address VARCHAR(300) NULL,
    road_address VARCHAR(300) NULL,
    building_name VARCHAR(200) NULL,
    dong_name VARCHAR(100) NULL,
    register_kind_code VARCHAR(20) NULL,
    register_kind_name VARCHAR(50) NULL,
    main_attachment_code VARCHAR(20) NULL,
    main_attachment_name VARCHAR(50) NULL,
    main_use_code VARCHAR(20) NULL,
    main_use_name VARCHAR(100) NULL,
    other_use VARCHAR(300) NULL,
    site_area_sqm DECIMAL(20, 4) NULL,
    building_area_sqm DECIMAL(20, 4) NULL,
    gross_floor_area_sqm DECIMAL(20, 4) NULL,
    building_coverage_ratio DECIMAL(12, 4) NULL,
    floor_area_ratio DECIMAL(12, 4) NULL,
    ground_floor_count INT NULL,
    basement_floor_count INT NULL,
    approval_date DATE NULL,
    parking_count INT NOT NULL DEFAULT 0,
    elevator_count INT NOT NULL DEFAULT 0,
    source_created_date DATE NULL,
    source_row_json JSON NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_molit_building_snapshot (
        building_register_id,
        snapshot_date
    ),
    INDEX idx_molit_building_legal_snapshot (
        legal_dong_id,
        snapshot_date
    ),
    INDEX idx_molit_building_use_snapshot (
        main_use_code,
        snapshot_date
    ),
    CONSTRAINT fk_molit_building_payload
        FOREIGN KEY (raw_api_payload_id) REFERENCES raw_api_payloads (id),
    CONSTRAINT fk_molit_building_legal_dong
        FOREIGN KEY (legal_dong_id) REFERENCES legal_dongs (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE building_region_mappings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source_building_id BIGINT NOT NULL,
    region_id BIGINT NULL,
    mapping_status VARCHAR(30) NOT NULL,
    mapping_confidence DECIMAL(5, 4) NOT NULL,
    mapping_rule VARCHAR(100) NOT NULL,
    candidate_count INT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_building_region_source (source_building_id),
    INDEX idx_building_region_region (region_id, mapping_status),
    CONSTRAINT fk_building_region_source
        FOREIGN KEY (source_building_id)
        REFERENCES source_molit_building_registers (id),
    CONSTRAINT fk_building_region_region
        FOREIGN KEY (region_id) REFERENCES regions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE region_built_environment_features
    ADD INDEX idx_built_environment_region_fk (region_id),
    ADD INDEX idx_built_environment_legal_fk (legal_dong_id),
    ADD INDEX idx_built_environment_period_fk (metric_period_id);

ALTER TABLE region_built_environment_features
    DROP INDEX uk_region_built_environment_features,
    ADD COLUMN snapshot_date DATE NULL AFTER metric_period_id,
    ADD COLUMN scope_key VARCHAR(100) NULL AFTER snapshot_date,
    ADD COLUMN region_level VARCHAR(30) NOT NULL DEFAULT 'LEGACY'
        AFTER scope_key,
    ADD COLUMN total_building_count INT NOT NULL DEFAULT 0
        AFTER region_level,
    MODIFY COLUMN commercial_building_count INT NOT NULL DEFAULT 0,
    MODIFY COLUMN avg_building_age DECIMAL(10, 4) NULL,
    ADD COLUMN aged_building_ratio DECIMAL(8, 4) NULL
        AFTER avg_building_age,
    MODIFY COLUMN avg_gross_floor_area DECIMAL(20, 4) NULL,
    CHANGE COLUMN parking_capacity_proxy
        parking_spaces_per_commercial_building DECIMAL(16, 4) NULL,
    ADD COLUMN total_parking_count INT NOT NULL DEFAULT 0
        AFTER avg_gross_floor_area,
    MODIFY COLUMN commercial_floor_area_proxy DECIMAL(24, 4) NULL,
    ADD COLUMN commercial_floor_area_ratio DECIMAL(8, 4) NULL
        AFTER commercial_floor_area_proxy,
    ADD COLUMN physical_environment_score DECIMAL(8, 4) NULL
        AFTER commercial_floor_area_ratio,
    ADD COLUMN source_building_count INT NOT NULL DEFAULT 0
        AFTER physical_environment_score,
    ADD COLUMN calculation_version VARCHAR(40) NOT NULL DEFAULT 'legacy'
        AFTER source_building_count;

UPDATE region_built_environment_features feature
JOIN metric_periods period ON period.id = feature.metric_period_id
SET feature.snapshot_date = period.end_date,
    feature.scope_key = CONCAT('LEGACY:', feature.id)
WHERE feature.snapshot_date IS NULL OR feature.scope_key IS NULL;

ALTER TABLE region_built_environment_features
    MODIFY COLUMN snapshot_date DATE NOT NULL,
    MODIFY COLUMN scope_key VARCHAR(100) NOT NULL,
    ADD UNIQUE KEY uk_built_environment_scope (
        scope_key,
        metric_period_id,
        snapshot_date
    ),
    ADD INDEX idx_built_environment_region_period (
        region_id,
        metric_period_id,
        snapshot_date
    ),
    ADD INDEX idx_built_environment_legal_period (
        legal_dong_id,
        metric_period_id,
        snapshot_date
    );
