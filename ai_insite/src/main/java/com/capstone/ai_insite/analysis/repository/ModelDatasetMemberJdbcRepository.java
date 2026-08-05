package com.capstone.ai_insite.analysis.repository;

import com.capstone.ai_insite.analysis.domain.DatasetSplit;
import java.sql.Date;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ModelDatasetMemberJdbcRepository {

    private static final String INSERT_MEMBERS = """
        INSERT INTO model_dataset_members (
            model_dataset_build_id,
            model_feature_snapshot_id,
            dataset_split
        )
        SELECT
            ?,
            feature.id,
            CASE
                WHEN horizon_period.end_date <= ? THEN 'TRAIN'
                WHEN horizon_period.end_date <= ? THEN 'VALIDATION'
                ELSE 'TEST'
            END
        FROM model_feature_snapshots feature
        JOIN metric_periods feature_period
            ON feature_period.id = feature.metric_period_id
        JOIN metric_periods horizon_period
            ON horizon_period.id = feature.label_horizon_period_id
        WHERE feature.feature_version = ?
            AND feature.label_version = ?
            AND feature.label_status = 'READY'
            AND feature_period.start_date BETWEEN ? AND ?
            AND feature.feature_as_of_date < horizon_period.start_date
            AND horizon_period.end_date <= ?
        ON DUPLICATE KEY UPDATE
            dataset_split = VALUES(dataset_split)
        """;

    private final JdbcTemplate jdbcTemplate;

    public ModelDatasetMemberJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<DatasetSplit, Integer> createMembers(
        Long datasetId,
        String featureVersion,
        String labelVersion,
        LocalDate featureFrom,
        LocalDate featureTo,
        LocalDate trainThrough,
        LocalDate validationThrough,
        LocalDate testThrough
    ) {
        jdbcTemplate.update(
            INSERT_MEMBERS,
            datasetId,
            Date.valueOf(trainThrough),
            Date.valueOf(validationThrough),
            featureVersion,
            labelVersion,
            Date.valueOf(featureFrom),
            Date.valueOf(featureTo),
            Date.valueOf(testThrough)
        );
        return jdbcTemplate.query(
            """
                SELECT dataset_split, COUNT(*) AS row_count
                FROM model_dataset_members
                WHERE model_dataset_build_id = ?
                GROUP BY dataset_split
                """,
            resultSet -> {
                Map<DatasetSplit, Integer> result = new EnumMap<>(DatasetSplit.class);
                while (resultSet.next()) {
                    result.put(
                        DatasetSplit.valueOf(resultSet.getString("dataset_split")),
                        resultSet.getInt("row_count")
                    );
                }
                return result;
            },
            datasetId
        );
    }
}
