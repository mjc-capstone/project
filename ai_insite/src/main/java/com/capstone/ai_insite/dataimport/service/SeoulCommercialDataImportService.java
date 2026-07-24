package com.capstone.ai_insite.dataimport.service;

import com.capstone.ai_insite.dataimport.domain.SalesImportCommand;
import com.capstone.ai_insite.dataimport.domain.SeoulCollectionPage;
import com.capstone.ai_insite.dataimport.domain.SeoulCommercialImportResult;
import com.capstone.ai_insite.dataimport.domain.SeoulQuarter;
import com.capstone.ai_insite.dataimport.domain.StoreImportCommand;
import com.capstone.ai_insite.dataimport.dto.seoul.SeoulApiPage;
import com.capstone.ai_insite.dataimport.dto.seoul.SeoulSalesApiRow;
import com.capstone.ai_insite.dataimport.dto.seoul.SeoulStoresApiRow;
import com.capstone.ai_insite.dataimport.mapper.SeoulOpenApiPageParser;
import com.capstone.ai_insite.dataimport.mapper.SeoulSalesRowMapper;
import com.capstone.ai_insite.dataimport.mapper.SeoulStoresRowMapper;
import com.capstone.ai_insite.metric.service.MetricBatchAggregationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "external.seoul.enabled", havingValue = "true")
public class SeoulCommercialDataImportService {

    private final SeoulRawCollectionService collectionService;
    private final SeoulOpenApiPageParser pageParser;
    private final SeoulSalesRowMapper salesMapper;
    private final SeoulStoresRowMapper storesMapper;
    private final SalesImportService salesImportService;
    private final StoreImportService storeImportService;
    private final MetricBatchAggregationService aggregationService;
    private final int pageSize;
    private final int maxPages;

    public SeoulCommercialDataImportService(
        SeoulRawCollectionService collectionService,
        SeoulOpenApiPageParser pageParser,
        SeoulSalesRowMapper salesMapper,
        SeoulStoresRowMapper storesMapper,
        SalesImportService salesImportService,
        StoreImportService storeImportService,
        MetricBatchAggregationService aggregationService,
        @Value("${external.seoul.page-size}") int pageSize,
        @Value("${external.seoul.max-pages}") int maxPages
    ) {
        this.collectionService = collectionService;
        this.pageParser = pageParser;
        this.salesMapper = salesMapper;
        this.storesMapper = storesMapper;
        this.salesImportService = salesImportService;
        this.storeImportService = storeImportService;
        this.aggregationService = aggregationService;
        this.pageSize = pageSize;
        this.maxPages = maxPages;
    }

    public SeoulCommercialImportResult importQuarter(String sourcePeriodCode) {
        SeoulQuarter quarter = SeoulQuarter.parse(sourcePeriodCode);
        ImportStats sales = importSales(sourcePeriodCode);
        ImportStats stores = importStores(sourcePeriodCode);
        int metricCount = aggregationService.aggregatePeriod(quarter.periodCode());
        return new SeoulCommercialImportResult(
            sourcePeriodCode,
            quarter.periodCode(),
            sales.pageCount(),
            sales.rowCount(),
            stores.pageCount(),
            stores.rowCount(),
            metricCount
        );
    }

    private ImportStats importSales(String sourcePeriodCode) {
        int importedRows = 0;
        for (int page = 0; page < maxPages; page++) {
            PageRange range = range(page);
            SeoulCollectionPage collected = collectionService.collect(
                SeoulOpenApiPageParser.SALES_SERVICE,
                range.start(),
                range.end(),
                sourcePeriodCode
            );
            SeoulApiPage<SeoulSalesApiRow> parsed = pageParser.parseSales(
                collected.responseBody()
            );
            List<SalesImportCommand> commands = parsed.rows().stream()
                .map(salesMapper::toCommand)
                .toList();
            importedRows += salesImportService.importBatch(
                collected.rawPayloadId(),
                commands
            );
            if (lastPage(collected, range)) {
                return new ImportStats(page + 1, importedRows);
            }
        }
        throw maxPagesExceeded(SeoulOpenApiPageParser.SALES_SERVICE);
    }

    private ImportStats importStores(String sourcePeriodCode) {
        int importedRows = 0;
        for (int page = 0; page < maxPages; page++) {
            PageRange range = range(page);
            SeoulCollectionPage collected = collectionService.collect(
                SeoulOpenApiPageParser.STORES_SERVICE,
                range.start(),
                range.end(),
                sourcePeriodCode
            );
            SeoulApiPage<SeoulStoresApiRow> parsed = pageParser.parseStores(
                collected.responseBody()
            );
            List<StoreImportCommand> commands = parsed.rows().stream()
                .map(storesMapper::toCommand)
                .toList();
            importedRows += storeImportService.importBatch(
                collected.rawPayloadId(),
                commands
            );
            if (lastPage(collected, range)) {
                return new ImportStats(page + 1, importedRows);
            }
        }
        throw maxPagesExceeded(SeoulOpenApiPageParser.STORES_SERVICE);
    }

    private PageRange range(int zeroBasedPage) {
        int start = zeroBasedPage * pageSize + 1;
        return new PageRange(start, start + pageSize - 1);
    }

    private boolean lastPage(SeoulCollectionPage page, PageRange range) {
        return page.rowCount() < pageSize
            || (page.totalCount() > 0 && range.end() >= page.totalCount());
    }

    private static IllegalStateException maxPagesExceeded(String serviceName) {
        return new IllegalStateException(
            "서울 API 최대 페이지 수를 초과했습니다: " + serviceName
        );
    }

    private record PageRange(int start, int end) {
    }

    private record ImportStats(int pageCount, int rowCount) {
    }
}
