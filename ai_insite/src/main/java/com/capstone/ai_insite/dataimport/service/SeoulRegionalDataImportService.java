package com.capstone.ai_insite.dataimport.service;

import com.capstone.ai_insite.dataimport.domain.SeoulCollectionPage;
import com.capstone.ai_insite.dataimport.domain.SeoulQuarter;
import com.capstone.ai_insite.dataimport.domain.SeoulRegionalImportCommand;
import com.capstone.ai_insite.dataimport.domain.SeoulRegionalImportResult;
import com.capstone.ai_insite.dataimport.dto.seoul.SeoulApiRow;
import com.capstone.ai_insite.dataimport.dto.seoul.SeoulRegionalApiRow;
import com.capstone.ai_insite.dataimport.mapper.SeoulOpenApiPageParser;
import com.capstone.ai_insite.dataimport.mapper.SeoulRegionalRowMapper;
import com.capstone.ai_insite.metric.service.MetricBatchAggregationService;
import com.capstone.ai_insite.metric.service.RegionPeriodFeatureAggregationService;
import java.util.List;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "external.seoul.enabled", havingValue = "true")
public class SeoulRegionalDataImportService {

    private final SeoulRawCollectionService collectionService;
    private final SeoulOpenApiPageParser pageParser;
    private final SeoulRegionalRowMapper rowMapper;
    private final SeoulRegionalSourceImportService sourceImportService;
    private final RegionPeriodFeatureAggregationService featureAggregationService;
    private final MetricBatchAggregationService metricAggregationService;
    private final int pageSize;
    private final int maxPages;

    public SeoulRegionalDataImportService(
        SeoulRawCollectionService collectionService,
        SeoulOpenApiPageParser pageParser,
        SeoulRegionalRowMapper rowMapper,
        SeoulRegionalSourceImportService sourceImportService,
        RegionPeriodFeatureAggregationService featureAggregationService,
        MetricBatchAggregationService metricAggregationService,
        @Value("${external.seoul.page-size}") int pageSize,
        @Value("${external.seoul.max-pages}") int maxPages
    ) {
        this.collectionService = collectionService;
        this.pageParser = pageParser;
        this.rowMapper = rowMapper;
        this.sourceImportService = sourceImportService;
        this.featureAggregationService = featureAggregationService;
        this.metricAggregationService = metricAggregationService;
        this.pageSize = pageSize;
        this.maxPages = maxPages;
    }

    public SeoulRegionalImportResult importQuarter(String sourcePeriodCode) {
        SeoulQuarter quarter = SeoulQuarter.parse(sourcePeriodCode);
        ImportStats floating = importSource(
            SeoulOpenApiPageParser.FLOATING_POPULATION_SERVICE,
            sourcePeriodCode,
            false,
            rowMapper::toFloatingPopulation,
            sourceImportService::importFloatingPopulation
        );
        ImportStats resident = importSource(
            SeoulOpenApiPageParser.RESIDENT_POPULATION_SERVICE,
            sourcePeriodCode,
            false,
            rowMapper::toResidentPopulation,
            sourceImportService::importResidentPopulation
        );
        ImportStats working = importSource(
            SeoulOpenApiPageParser.WORKING_POPULATION_SERVICE,
            sourcePeriodCode,
            true,
            rowMapper::toWorkingPopulation,
            sourceImportService::importWorkingPopulation
        );
        ImportStats facilities = importSource(
            SeoulOpenApiPageParser.FACILITIES_SERVICE,
            sourcePeriodCode,
            false,
            rowMapper::toFacilities,
            sourceImportService::importFacilities
        );
        ImportStats apartments = importSource(
            SeoulOpenApiPageParser.APARTMENTS_SERVICE,
            sourcePeriodCode,
            false,
            rowMapper::toApartments,
            sourceImportService::importApartments
        );
        int featureCount = featureAggregationService.aggregatePeriod(quarter.periodCode());
        int metricCount = metricAggregationService.aggregatePeriod(quarter.periodCode());
        return new SeoulRegionalImportResult(
            sourcePeriodCode,
            quarter.periodCode(),
            floating.pageCount(),
            floating.rowCount(),
            resident.pageCount(),
            resident.rowCount(),
            working.pageCount(),
            working.rowCount(),
            facilities.pageCount(),
            facilities.rowCount(),
            apartments.pageCount(),
            apartments.rowCount(),
            featureCount,
            metricCount
        );
    }

    private <C extends SeoulRegionalImportCommand> ImportStats importSource(
        String serviceName,
        String sourcePeriodCode,
        boolean serverPeriodFilter,
        Function<SeoulApiRow<SeoulRegionalApiRow>, C> mapper,
        BatchImporter<C> importer
    ) {
        int importedRows = 0;
        for (int page = 0; page < maxPages; page++) {
            PageRange range = range(page);
            SeoulCollectionPage collected = serverPeriodFilter
                ? collectionService.collect(
                    serviceName,
                    range.start(),
                    range.end(),
                    sourcePeriodCode
                )
                : collectionService.collectHistory(
                    serviceName,
                    range.start(),
                    range.end(),
                    sourcePeriodCode
                );
            List<C> commands = pageParser
                .parseRegional(collected.responseBody(), serviceName)
                .rows()
                .stream()
                .filter(row -> sourcePeriodCode.equals(
                    row.value().getSourcePeriodCode()
                ))
                .map(mapper)
                .toList();
            importedRows += importer.importBatch(collected.rawPayloadId(), commands);
            if (lastPage(collected, range)) {
                if (importedRows == 0) {
                    throw new IllegalStateException(
                        "서울 API에 대상 분기 데이터가 없습니다: "
                            + serviceName + ", " + sourcePeriodCode
                    );
                }
                return new ImportStats(page + 1, importedRows);
            }
        }
        throw new IllegalStateException(
            "서울 API 최대 페이지 수를 초과했습니다: " + serviceName
        );
    }

    private PageRange range(int zeroBasedPage) {
        int start = zeroBasedPage * pageSize + 1;
        return new PageRange(start, start + pageSize - 1);
    }

    private boolean lastPage(SeoulCollectionPage page, PageRange range) {
        return page.rowCount() < pageSize
            || (page.totalCount() > 0 && range.end() >= page.totalCount());
    }

    @FunctionalInterface
    private interface BatchImporter<C> {

        int importBatch(Long rawPayloadId, List<C> commands);
    }

    private record PageRange(int start, int end) {
    }

    private record ImportStats(int pageCount, int rowCount) {
    }
}
