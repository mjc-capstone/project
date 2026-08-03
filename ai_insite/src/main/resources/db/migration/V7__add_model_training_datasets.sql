ALTER TABLE model_feature_snapshots
    ADD COLUMN feature_as_of_date DATE NULL AFTER metric_period_id,
    ADD COLUMN label_period_id BIGINT NULL AFTER label_json,
    ADD COLUMN label_status VARCHAR(30) NOT NULL DEFAULT 'PENDING' AFTER label_period_id,
    ADD COLUMN label_version VARCHAR(30) NULL AFTER label_status,
    ADD COLUMN labeled_at DATETIME(6) NULL AFTER label_version;

UPDATE model_feature_snapshots snapshot
JOIN metric_periods period ON period.id = snapshot.metric_period_id
SET snapshot.feature_as_of_date = period.end_date
WHERE snapshot.feature_as_of_date IS NULL;

ALTER TABLE model_feature_snapshots
    MODIFY COLUMN feature_as_of_date DATE NOT NULL,
    ADD INDEX idx_model_feature_label_status_version (label_status, label_version),
    ADD INDEX idx_model_feature_label_period (label_period_id),
    ADD CONSTRAINT fk_model_feature_label_period
        FOREIGN KEY (label_period_id) REFERENCES metric_periods (id);

CREATE TABLE model_dataset_builds (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dataset_version VARCHAR(50) NOT NULL,
    feature_version VARCHAR(30) NOT NULL,
    label_version VARCHAR(30) NOT NULL,
    feature_from_period_id BIGINT NOT NULL,
    train_through_period_id BIGINT NOT NULL,
    validation_through_period_id BIGINT NOT NULL,
    test_through_period_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    eligible_feature_count INT NOT NULL DEFAULT 0,
    train_example_count INT NOT NULL DEFAULT 0,
    validation_example_count INT NOT NULL DEFAULT 0,
    test_example_count INT NOT NULL DEFAULT 0,
    model_version VARCHAR(50) NULL,
    evaluation_metrics_json JSON NULL,
    completed_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_model_dataset_builds_version (dataset_version),
    CONSTRAINT fk_dataset_feature_from_period
        FOREIGN KEY (feature_from_period_id) REFERENCES metric_periods (id),
    CONSTRAINT fk_dataset_train_through_period
        FOREIGN KEY (train_through_period_id) REFERENCES metric_periods (id),
    CONSTRAINT fk_dataset_validation_through_period
        FOREIGN KEY (validation_through_period_id) REFERENCES metric_periods (id),
    CONSTRAINT fk_dataset_test_through_period
        FOREIGN KEY (test_through_period_id) REFERENCES metric_periods (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE model_dataset_members (
    id BIGINT NOT NULL AUTO_INCREMENT,
    model_dataset_build_id BIGINT NOT NULL,
    model_feature_snapshot_id BIGINT NOT NULL,
    dataset_split VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_model_dataset_member (model_dataset_build_id, model_feature_snapshot_id),
    INDEX idx_model_dataset_member_split (model_dataset_build_id, dataset_split),
    CONSTRAINT fk_dataset_member_build
        FOREIGN KEY (model_dataset_build_id) REFERENCES model_dataset_builds (id),
    CONSTRAINT fk_dataset_member_feature
        FOREIGN KEY (model_feature_snapshot_id) REFERENCES model_feature_snapshots (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
