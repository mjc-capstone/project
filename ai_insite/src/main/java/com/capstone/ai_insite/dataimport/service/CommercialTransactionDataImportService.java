package com.capstone.ai_insite.dataimport.service;

import com.capstone.ai_insite.dataimport.client.PublicDataApiClient;
import com.capstone.ai_insite.dataimport.domain.CostDataImportResult;
import com.capstone.ai_insite.dataimport.domain.DataImportJobProgress;
import com.capstone.ai_insite.dataimport.domain.RawPayloadCommand;
import com.capstone.ai_insite.dataimport.domain.SeoulQuarter;
import com.capstone.ai_insite.dataimport.dto.publicdata.CollectedCommercialTransaction;
import com.capstone.ai_insite.dataimport.dto.publicdata.CommercialTransactionPage;
import com.capstone.ai_insite.dataimport.entity.DataImportJobEntity;
import com.capstone.ai_insite.dataimport.entity.RawApiPayloadEntity;
import com.capstone.ai_insite.dataimport.mapper.CommercialTransactionXmlParser;
import com.capstone.ai_insite.metric.entity.MetricPeriodEntity;
import com.capstone.ai_insite.metric.service.CommercialTransactionCostFeatureAggregationService;
import com.capstone.ai_insite.region.repository.RegionJpaRepository;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@ConditionalOnProperty(name = "external.public-data.enabled", havingValue = "true")
public class CommercialTransactionDataImportService {

    private static final String SOURCE_NAME = "MOLIT";
    private static final String SERVICE_NAME = "commercialRealEstateTrade";

    private final PublicDataApiClient apiClient;
    private final CommercialTransactionXmlParser parser;
    private final DataImportJobService jobService;
    private final RawPayloadService rawPayloadService;
    private final ImportMasterResolver masterResolver;
    private final CostSourcePersistenceService persistenceService;
    private final CommercialTransactionCostFeatureAggregationService aggregationService;
    private final RegionJpaRepository regionRepository;
    private final ObjectMapper objectMapper;
    private final String path;
    private final int pageSize;

    public CommercialTransactionDataImportService(
        PublicDataApiClient apiClient,
        CommercialTransactionXmlParser parser,
        DataImportJobService jobService,
        RawPayloadService rawPayloadService,
        ImportMasterResolver masterResolver,
        CostSourcePersistenceService persistenceService,
        CommercialTransactionCostFeatureAggregationService aggregationService,
        RegionJpaRepository regionRepository,
        ObjectMapper objectMapper,
        @Value("${external.public-data.commercial-transaction.path}") String path,
        @Value("${external.public-data.commercial-transaction.page-size}") int pageSize
    ) {
        this.apiClient = apiClient;
        this.parser = parser;
        this.jobService = jobService;
        this.rawPayloadService = rawPayloadService;
        this.masterResolver = masterResolver;
        this.persistenceService = persistenceService;
        this.aggregationService = aggregationService;
        this.regionRepository = regionRepository;
        this.objectMapper = objectMapper;
        this.path = path;
        this.pageSize = pageSize;
    }

    public CostDataImportResult importQuarter(
        String sourcePeriodCode,
        String requestedBy,
        Long retryOfJobId
    ) {
        SeoulQuarter quarter = SeoulQuarter.parse(sourcePeriodCode);
        MetricPeriodEntity period = masterResolver.period(quarter.periodCode());
        DataImportJobEntity job = jobService.start(
            SOURCE_NAME,
            SERVICE_NAME,
            quarter.periodCode(),
            requestedBy,
            retryOfJobId
        );
        int pages = 0;
        int fetched = 0;
        try {
            List<String> districtCodes = districtCodes();
            List<CollectedCommercialTransaction> collected = new ArrayList<>();
            YearMonth firstMonth = YearMonth.from(period.getStartDate());
            for (String districtCode : districtCodes) {
                for (int monthOffset = 0; monthOffset < 3; monthOffset++) {
                    String dealMonth = firstMonth.plusMonths(monthOffset)
                        .toString()
                        .replace("-", "");
                    int pageNumber = 1;
                    int totalPages;
                    do {
                        Map<String, String> parameters = parameters(
                            districtCode,
                            dealMonth,
                            pageNumber
                        );
                        String response = apiClient.fetch(path, parameters);
                        CommercialTransactionPage parsed = parser.parse(response);
                        RawApiPayloadEntity payload = rawPayloadService.save(
                            new RawPayloadCommand(
                                SOURCE_NAME,
                                SERVICE_NAME,
                                path,
                                json(parameters),
                                json(Map.of("xml", response)),
                                parsed.rows().size(),
                                job.getId()
                            )
                        );
                        parsed.rows().forEach(row -> collected.add(
                            new CollectedCommercialTransaction(payload, row)
                        ));
                        pages++;
                        fetched += parsed.rows().size();
                        totalPages = Math.max(
                            1,
                            (parsed.totalCount() + pageSize - 1) / pageSize
                        );
                        pageNumber++;
                    } while (pageNumber <= totalPages);
                }
            }
            int persisted = persistenceService.replaceTransactions(
                period,
                districtCodes,
                collected
            );
            int generated = aggregationService.rebuild(period);
            DataImportJobProgress progress = new DataImportJobProgress(
                pages,
                fetched,
                persisted,
                fetched - persisted
            );
            jobService.complete(job.getId(), progress);
            return new CostDataImportResult(
                job.getId(),
                quarter.periodCode(),
                pages,
                fetched,
                persisted,
                fetched - persisted,
                generated
            );
        } catch (RuntimeException exception) {
            jobService.fail(job.getId(), exception);
            throw exception;
        }
    }

    private List<String> districtCodes() {
        return regionRepository.findByActiveTrue().stream()
            .map(region -> region.getSigunguCode())
            .filter(Objects::nonNull)
            .filter(code -> code.matches("11\\d{3}"))
            .distinct()
            .sorted()
            .toList();
    }

    private Map<String, String> parameters(
        String districtCode,
        String dealMonth,
        int pageNumber
    ) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("LAWD_CD", districtCode);
        parameters.put("DEAL_YMD", dealMonth);
        parameters.put("numOfRows", String.valueOf(pageSize));
        parameters.put("pageNo", String.valueOf(pageNumber));
        return parameters;
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("JSON serialization failed.", exception);
        }
    }
}
