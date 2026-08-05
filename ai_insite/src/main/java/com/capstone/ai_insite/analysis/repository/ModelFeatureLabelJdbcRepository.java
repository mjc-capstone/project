package com.capstone.ai_insite.analysis.repository;

import com.capstone.ai_insite.analysis.domain.FeatureLabelBuildResult;
import com.capstone.ai_insite.analysis.service.FeatureBuildService;
import com.capstone.ai_insite.analysis.service.ModelFeatureLabelService;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ModelFeatureLabelJdbcRepository {

    private static final String UPSERT_FEATURES = """
        INSERT INTO model_feature_snapshots (
            region_id, business_category_id, metric_period_id,
            feature_as_of_date, feature_json, label_status, feature_version
        )
        SELECT
            metric.region_id,
            metric.business_category_id,
            metric.metric_period_id,
            period.end_date,
            JSON_OBJECT(
                'sourceMetricSnapshotId', metric.id,
                'featureAsOfDate', DATE_FORMAT(period.end_date, '%Y-%m-%d'),
                'salesAmount', metric.sales_amount,
                'salesCount', metric.sales_count,
                'salesGrowthRateQoq', metric.sales_growth_rate_qoq,
                'storeCount', metric.store_count,
                'storeGrowthRateQoq', metric.store_growth_rate_qoq,
                'floatingPopulation', regional.floating_population_total,
                'residentPopulation', regional.resident_population_total,
                'workingPopulation', regional.working_population_total,
                'demandScore', metric.demand_score,
                'competitionScore', metric.competition_intensity_score,
                'marketScore', metric.market_score,
                'stabilityScore', metric.stability_score,
                'closureRiskSignal', metric.closure_risk_signal,
                'activeStoreCount', competition.active_store_count,
                'sameCategoryStoreCount', competition.same_category_store_count,
                'storeCountPerSquareKm', competition.store_count_per_square_km,
                'franchiseStoreCountFromRegistry', competition.franchise_store_count,
                'categoryDiversityIndex', competition.category_diversity_index,
                'competitionSnapshotDate', competition.snapshot_date,
                'registeredVsSeoulStoreCountDifference',
                    CASE
                        WHEN competition.id IS NULL OR metric.store_count IS NULL THEN NULL
                        ELSE competition.same_category_store_count - metric.store_count
                    END,
                'rentAmountPerSquareMeter', rent.rent_amount,
                'rentIndex', rent.rent_index,
                'vacancyRate', rent.vacancy_rate,
                'investmentReturnRate', rent.investment_return_rate,
                'rentPressureScore', rent.rent_pressure_score,
                'vacancyRiskScore', rent.vacancy_risk_score,
                'fixedCostBurdenIndex', rent.fixed_cost_burden_index,
                'commercialTransactionCount', transaction_cost.commercial_transaction_count,
                'medianCommercialPricePerArea', transaction_cost.median_commercial_price_per_area,
                'commercialPriceGrowthRate', transaction_cost.price_growth_rate,
                'locationCostScore', transaction_cost.location_cost_score,
                'totalBuildingCount', building.total_building_count,
                'commercialBuildingCount', building.commercial_building_count,
                'averageBuildingAge', building.avg_building_age,
                'agedBuildingRatio', building.aged_building_ratio,
                'averageGrossFloorArea', building.avg_gross_floor_area,
                'totalParkingCount', building.total_parking_count,
                'parkingSpacesPerCommercialBuilding', building.parking_spaces_per_commercial_building,
                'commercialFloorAreaProxy', building.commercial_floor_area_proxy,
                'commercialFloorAreaRatio', building.commercial_floor_area_ratio,
                'physicalEnvironmentScore', building.physical_environment_score
            ),
            'PENDING',
            ?
        FROM commercial_metric_snapshots metric
        JOIN metric_periods period ON period.id = metric.metric_period_id
        LEFT JOIN region_period_features regional
            ON regional.region_id = metric.region_id
            AND regional.metric_period_id = metric.metric_period_id
        LEFT JOIN (
            SELECT ranked.*
            FROM (
                SELECT source.*,
                    ROW_NUMBER() OVER (
                        PARTITION BY region_id, business_category_id, metric_period_id
                        ORDER BY snapshot_date DESC, id DESC
                    ) AS row_number_in_group
                FROM commercial_competition_features source
            ) ranked
            WHERE ranked.row_number_in_group = 1
        ) competition
            ON competition.region_id = metric.region_id
            AND competition.business_category_id = metric.business_category_id
            AND competition.metric_period_id = metric.metric_period_id
            AND competition.snapshot_date <= period.end_date
        LEFT JOIN region_cost_features transaction_cost
            ON transaction_cost.region_id = metric.region_id
            AND transaction_cost.metric_period_id = metric.metric_period_id
            AND transaction_cost.source_system = 'MOLIT'
            AND transaction_cost.property_type = 'ALL_COMMERCIAL'
        LEFT JOIN region_cost_features rent
            ON rent.metric_period_id = metric.metric_period_id
            AND rent.source_system = 'REB'
            AND rent.region_level = 'SIDO'
            AND rent.property_type = 'SMALL_RETAIL'
        LEFT JOIN (
            SELECT ranked.*
            FROM (
                SELECT source.*,
                    ROW_NUMBER() OVER (
                        PARTITION BY region_id, metric_period_id
                        ORDER BY snapshot_date DESC, id DESC
                    ) AS row_number_in_group
                FROM region_built_environment_features source
                WHERE region_id IS NOT NULL
            ) ranked
            WHERE ranked.row_number_in_group = 1
        ) building
            ON building.region_id = metric.region_id
            AND building.metric_period_id = metric.metric_period_id
        WHERE period.start_date BETWEEN ? AND ?
        ON DUPLICATE KEY UPDATE
            feature_as_of_date = VALUES(feature_as_of_date),
            feature_json = VALUES(feature_json)
        """;

    private static final String UPDATE_LABELS = """
        UPDATE model_feature_snapshots feature
        JOIN metric_periods current_period
            ON current_period.id = feature.metric_period_id
        JOIN commercial_metric_snapshots current_metric
            ON current_metric.region_id = feature.region_id
            AND current_metric.business_category_id = feature.business_category_id
            AND current_metric.metric_period_id = feature.metric_period_id
        LEFT JOIN metric_periods next_period
            ON next_period.start_date = DATE_ADD(current_period.start_date, INTERVAL 3 MONTH)
            AND next_period.period_type = 'QUARTER'
        LEFT JOIN commercial_metric_snapshots next_metric
            ON next_metric.region_id = feature.region_id
            AND next_metric.business_category_id = feature.business_category_id
            AND next_metric.metric_period_id = next_period.id
        LEFT JOIN metric_periods horizon_period
            ON horizon_period.start_date = DATE_ADD(current_period.start_date, INTERVAL 12 MONTH)
            AND horizon_period.period_type = 'QUARTER'
        LEFT JOIN commercial_metric_snapshots horizon_metric
            ON horizon_metric.region_id = feature.region_id
            AND horizon_metric.business_category_id = feature.business_category_id
            AND horizon_metric.metric_period_id = horizon_period.id
        SET
            feature.label_json = CASE
                WHEN current_metric.sales_amount > 0
                    AND next_metric.sales_amount IS NOT NULL
                    AND current_metric.store_count IS NOT NULL
                    AND next_metric.store_count IS NOT NULL
                    AND next_metric.close_rate IS NOT NULL
                THEN JSON_OBJECT(
                    'targetPeriodCode', next_period.period_code,
                    'nextQuarterSalesGrowthRate',
                        ROUND(
                            (next_metric.sales_amount - current_metric.sales_amount)
                            * 100.0 / current_metric.sales_amount,
                            4
                        ),
                    'nextQuarterStoreCountDeclined',
                        JSON_EXTRACT(
                            IF(next_metric.store_count < current_metric.store_count, 'true', 'false'),
                            '$'
                        ),
                    'nextQuarterCloseRate', next_metric.close_rate,
                    'fourQuarterTargetPeriodCode',
                        CASE WHEN horizon_metric.id IS NULL THEN NULL ELSE horizon_period.period_code END,
                    'fourQuarterStoreRetentionRate',
                        CASE
                            WHEN horizon_metric.id IS NULL
                                OR current_metric.store_count <= 0
                                OR horizon_metric.store_count IS NULL
                            THEN NULL
                            ELSE ROUND(
                                horizon_metric.store_count * 100.0 / current_metric.store_count,
                                4
                            )
                        END,
                    'fourQuarterStoreBaseMaintained',
                        CASE
                            WHEN horizon_metric.id IS NULL
                                OR current_metric.store_count <= 0
                                OR horizon_metric.store_count IS NULL
                            THEN NULL
                            ELSE JSON_EXTRACT(
                                IF(
                                    horizon_metric.store_count * 100.0 / current_metric.store_count >= 100,
                                    'true',
                                    'false'
                                ),
                                '$'
                            )
                        END
                )
                ELSE NULL
            END,
            feature.label_period_id = CASE
                WHEN next_metric.id IS NULL THEN NULL ELSE next_period.id
            END,
            feature.label_horizon_period_id = CASE
                WHEN next_metric.id IS NULL THEN NULL
                WHEN horizon_metric.id IS NOT NULL THEN horizon_period.id
                ELSE next_period.id
            END,
            feature.label_status = CASE
                WHEN next_metric.id IS NULL THEN 'MISSING_TARGET'
                WHEN current_metric.sales_amount IS NULL
                    OR current_metric.sales_amount <= 0
                    OR next_metric.sales_amount IS NULL
                    OR current_metric.store_count IS NULL
                    OR next_metric.store_count IS NULL
                    OR next_metric.close_rate IS NULL
                THEN 'INCOMPLETE_SOURCE'
                ELSE 'READY'
            END,
            feature.label_version = ?,
            feature.labeled_at = CURRENT_TIMESTAMP(6)
        WHERE feature.feature_version = ?
            AND current_period.start_date BETWEEN ? AND ?
        """;

    private final JdbcTemplate jdbcTemplate;

    public ModelFeatureLabelJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public FeatureLabelBuildResult rebuild(LocalDate from, LocalDate to) {
        jdbcTemplate.update(
            UPSERT_FEATURES,
            FeatureBuildService.FEATURE_VERSION,
            Date.valueOf(from),
            Date.valueOf(to)
        );
        jdbcTemplate.update(
            UPDATE_LABELS,
            ModelFeatureLabelService.LABEL_VERSION,
            FeatureBuildService.FEATURE_VERSION,
            Date.valueOf(from),
            Date.valueOf(to)
        );

        Map<String, Integer> counts = jdbcTemplate.query(
            """
                SELECT feature.label_status, COUNT(*) AS row_count
                FROM model_feature_snapshots feature
                JOIN metric_periods period ON period.id = feature.metric_period_id
                WHERE feature.feature_version = ?
                    AND period.start_date BETWEEN ? AND ?
                GROUP BY feature.label_status
                """,
            resultSet -> {
                Map<String, Integer> result = new java.util.HashMap<>();
                while (resultSet.next()) {
                    result.put(
                        resultSet.getString("label_status"),
                        resultSet.getInt("row_count")
                    );
                }
                return result;
            },
            FeatureBuildService.FEATURE_VERSION,
            Date.valueOf(from),
            Date.valueOf(to)
        );
        int ready = counts.getOrDefault("READY", 0);
        int missing = counts.getOrDefault("MISSING_TARGET", 0);
        int incomplete = counts.getOrDefault("INCOMPLETE_SOURCE", 0);
        return new FeatureLabelBuildResult(
            FeatureBuildService.FEATURE_VERSION,
            ModelFeatureLabelService.LABEL_VERSION,
            ready + missing + incomplete + counts.getOrDefault("PENDING", 0),
            ready,
            missing,
            incomplete
        );
    }
}
