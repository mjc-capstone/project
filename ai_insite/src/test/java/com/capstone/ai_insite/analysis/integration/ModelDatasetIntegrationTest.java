package com.capstone.ai_insite.analysis.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.capstone.ai_insite.analysis.domain.DatasetSplit;
import com.capstone.ai_insite.analysis.domain.ModelDatasetBuildCommand;
import com.capstone.ai_insite.analysis.domain.ModelDatasetBuildStatus;
import com.capstone.ai_insite.analysis.domain.ModelDatasetAuditStatus;
import com.capstone.ai_insite.analysis.domain.ModelEvaluationCommand;
import com.capstone.ai_insite.analysis.service.ModelDatasetApplicationService;
import com.capstone.ai_insite.analysis.service.ModelDatasetAuditService;
import com.capstone.ai_insite.analysis.service.ModelDatasetNdjsonExportService;
import com.capstone.ai_insite.category.entity.BusinessCategoryEntity;
import com.capstone.ai_insite.category.repository.BusinessCategoryJpaRepository;
import com.capstone.ai_insite.dataimport.domain.SalesImportCommand;
import com.capstone.ai_insite.dataimport.domain.StoreImportCommand;
import com.capstone.ai_insite.metric.entity.CommercialMetricSnapshotEntity;
import com.capstone.ai_insite.metric.entity.MetricPeriodEntity;
import com.capstone.ai_insite.metric.entity.SourceSeoulSalesEntity;
import com.capstone.ai_insite.metric.entity.SourceSeoulStoresEntity;
import com.capstone.ai_insite.metric.repository.CommercialMetricSnapshotJpaRepository;
import com.capstone.ai_insite.metric.repository.MetricPeriodJpaRepository;
import com.capstone.ai_insite.region.entity.RegionEntity;
import com.capstone.ai_insite.region.repository.RegionJpaRepository;
import java.math.BigDecimal;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@Transactional
class ModelDatasetIntegrationTest {

    @Autowired
    private ModelDatasetApplicationService datasetService;

    @Autowired
    private RegionJpaRepository regionRepository;

    @Autowired
    private BusinessCategoryJpaRepository categoryRepository;

    @Autowired
    private MetricPeriodJpaRepository periodRepository;

    @Autowired
    private CommercialMetricSnapshotJpaRepository metricRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelDatasetAuditService auditService;

    @Autowired
    private ModelDatasetNdjsonExportService exportService;

    @Test
    void buildsLeakageSafeTimeSplitsAndLabelsOnMysql() throws Exception {
        RegionEntity region = regionRepository.save(
            RegionEntity.createSeoulAdministrativeDong(
                "1199999999",
                "P5통합테스트동",
                "11999",
                "P5테스트구"
            )
        );
        BusinessCategoryEntity category = categoryRepository.save(
            BusinessCategoryEntity.createSeoulCommercial(
                "CS9P5001",
                "P5 통합 테스트 업종"
            )
        );
        List<MetricPeriodEntity> periods = periodRepository.saveAll(List.of(
            MetricPeriodEntity.createQuarter("2088Q1", 2088, 1),
            MetricPeriodEntity.createQuarter("2088Q2", 2088, 2),
            MetricPeriodEntity.createQuarter("2088Q3", 2088, 3),
            MetricPeriodEntity.createQuarter("2088Q4", 2088, 4)
        ));
        saveMetric(region, category, periods.get(0), 1_000L, 10, "2.0");
        saveMetric(region, category, periods.get(1), 1_200L, 9, "3.0");
        saveMetric(region, category, periods.get(2), 1_100L, 8, "4.0");
        saveMetric(region, category, periods.get(3), 1_300L, 11, "5.0");

        var result = datasetService.build(new ModelDatasetBuildCommand(
            "p5-integration-2088",
            "2088Q1",
            "2088Q2",
            "2088Q3",
            "2088Q4"
        ));

        assertEquals(ModelDatasetBuildStatus.READY, result.status());
        assertEquals(3, result.eligibleFeatureCount());
        assertEquals(1, result.trainExampleCount());
        assertEquals(1, result.validationExampleCount());
        assertEquals(1, result.testExampleCount());

        var evaluated = datasetService.recordEvaluation(
            result.id(),
            new ModelEvaluationCommand(
                "baseline-v1",
                "{\"salesGrowthRmse\":12.34}"
            )
        );
        assertEquals("baseline-v1", evaluated.modelVersion());
        assertTrue(evaluated.evaluationMetricsJson().contains("12.34"));

        var train = datasetService.getExamples(result.id(), DatasetSplit.TRAIN);
        assertEquals(1, train.size());
        assertEquals("2088Q1", train.getFirst().featurePeriod());
        assertEquals("2088Q2", train.getFirst().labelPeriod());
        assertEquals("2088Q2", train.getFirst().labelHorizonPeriod());
        assertTrue(train.getFirst().featureAsOfDate().isBefore(
            periods.get(1).getStartDate()
        ));
        assertFalse(train.getFirst().featureJson().contains("targetPeriodCode"));
        var labels = objectMapper.readTree(train.getFirst().labelJson());
        assertEquals("2088Q2", labels.get("targetPeriodCode").asString());
        assertEquals(
            0,
            new BigDecimal("20.0000").compareTo(
                labels.get("nextQuarterSalesGrowthRate").decimalValue()
            )
        );
        assertTrue(labels.get("fourQuarterStoreRetentionRate").isNull());

        var audit = auditService.audit(result.id());
        assertEquals(ModelDatasetAuditStatus.NOT_READY, audit.status());
        assertEquals(3, audit.totalExampleCount());
        assertEquals(1L, audit.splitCounts().get(DatasetSplit.TRAIN));
        assertTrue(audit.blockers().stream().anyMatch(
            blocker -> blocker.contains("최소 8개")
        ));
        assertTrue(audit.features().containsKey("salesAmount"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        exportService.export(result.id(), DatasetSplit.TRAIN, output);
        String[] rows = output.toString(StandardCharsets.UTF_8).strip().split("\\R");
        assertEquals(1, rows.length);
        var exported = objectMapper.readTree(rows[0]);
        assertEquals("TRAIN", exported.get("split").asString());
        assertEquals("2088Q1", exported.get("featurePeriod").asString());
        assertEquals("2088Q2", exported.get("labelPeriod").asString());
        assertTrue(exported.get("features").has("salesAmount"));
        assertTrue(exported.get("labels").has("nextQuarterSalesGrowthRate"));
    }

    private void saveMetric(
        RegionEntity region,
        BusinessCategoryEntity category,
        MetricPeriodEntity period,
        long salesAmount,
        int storeCount,
        String closeRate
    ) {
        SourceSeoulSalesEntity sales = new SourceSeoulSalesEntity(
            null,
            region,
            category,
            period
        );
        sales.apply(new SalesImportCommand(
            null,
            region.getAdministrativeDongCode(),
            region.getAdministrativeDongName(),
            category.getSourceCategoryCode(),
            category.getSourceCategoryName(),
            period.getPeriodCode(),
            period.getPeriodCode(),
            salesAmount,
            100L,
            null,
            null,
            null,
            null,
            null,
            "{}"
        ), null);
        SourceSeoulStoresEntity stores = new SourceSeoulStoresEntity(
            null,
            region,
            category,
            period
        );
        stores.apply(new StoreImportCommand(
            null,
            region.getAdministrativeDongCode(),
            region.getAdministrativeDongName(),
            category.getSourceCategoryCode(),
            category.getSourceCategoryName(),
            period.getPeriodCode(),
            period.getPeriodCode(),
            storeCount,
            storeCount,
            0,
            BigDecimal.ONE,
            1,
            new BigDecimal(closeRate),
            1,
            "{}"
        ), null);
        CommercialMetricSnapshotEntity metric = new CommercialMetricSnapshotEntity(
            region,
            category,
            period
        );
        metric.applySources(sales, stores, null, null);
        metricRepository.save(metric);
    }
}
