ALTER TABLE model_feature_snapshots
    ADD COLUMN label_horizon_period_id BIGINT NULL AFTER label_period_id;

UPDATE model_feature_snapshots
SET label_horizon_period_id = label_period_id
WHERE label_period_id IS NOT NULL;

ALTER TABLE model_feature_snapshots
    ADD INDEX idx_model_feature_label_horizon (label_horizon_period_id),
    ADD CONSTRAINT fk_model_feature_label_horizon
        FOREIGN KEY (label_horizon_period_id) REFERENCES metric_periods (id);
