ALTER TABLE commercial_metric_snapshots
    MODIFY COLUMN sales_growth_rate_qoq DECIMAL(24, 4) NULL,
    MODIFY COLUMN sales_growth_rate_yoy DECIMAL(24, 4) NULL,
    MODIFY COLUMN store_growth_rate_qoq DECIMAL(24, 4) NULL;
