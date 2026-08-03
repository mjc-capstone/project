package com.capstone.ai_insite.dataimport.service;

import com.capstone.ai_insite.dataimport.client.RebApiClient;
import com.capstone.ai_insite.dataimport.domain.CostDataImportResult;
import com.capstone.ai_insite.dataimport.domain.DataImportJobProgress;
import com.capstone.ai_insite.dataimport.domain.RawPayloadCommand;
import com.capstone.ai_insite.dataimport.domain.RebCommercialMetricType;
import com.capstone.ai_insite.dataimport.domain.RebCommercialPropertyType;
import com.capstone.ai_insite.dataimport.domain.SeoulQuarter;
import com.capstone.ai_insite.dataimport.dto.reb.CollectedRebCommercialRentObservation;
import com.capstone.ai_insite.dataimport.dto.reb.RebCommercialRentPage;
import com.capstone.ai_insite.dataimport.entity.DataImportJobEntity;
import com.capstone.ai_insite.dataimport.entity.RawApiPayloadEntity;
import com.capstone.ai_insite.dataimport.mapper.RebCommercialRentApiParser;
import com.capstone.ai_insite.metric.entity.MetricPeriodEntity;
import com.capstone.ai_insite.metric.service.RebCostFeatureAggregationService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@ConditionalOnProperty(name = "external.reb.enabled", havingValue = "true")
public class RebCommercialRentDataImportService {

    private static final String SOURCE_NAME = "REB";
    private static final String SERVICE_NAME = "commercialRentStatistics";

    private final RebApiClient apiClient;
    private final RebCommercialRentApiParser parser;
    private final DataImportJobService jobService;
    private final RawPayloadService rawPayloadService;
    private final ImportMasterResolver masterResolver;
    private final CostSourcePersistenceService persistenceService;
    private final RebCostFeatureAggregationService aggregationService;
    private final ObjectMapper objectMapper;
    private final int pageSize;

    public RebCommercialRentDataImportService(
        RebApiClient apiClient,
        RebCommercialRentApiParser parser,
        DataImportJobService jobService,
        RawPayloadService rawPayloadService,
        ImportMasterResolver masterResolver,
        CostSourcePersistenceService persistenceService,
        RebCostFeatureAggregationService aggregationService,
        ObjectMapper objectMapper,
        @Value("${external.reb.page-size}") int pageSize
    ) {
        this.apiClient = apiClient;
        this.parser = parser;
        this.jobService = jobService;
        this.rawPayloadService = rawPayloadService;
        this.masterResolver = masterResolver;
        this.persistenceService = persistenceService;
        this.aggregationService = aggregationService;
        this.objectMapper = objectMapper;
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
        int normalized = 0;
        try {
            List<CollectedRebCommercialRentObservation> collected =
                new ArrayList<>();
            String rebPeriod = "%04d%02d".formatted(
                quarter.year(),
                quarter.quarter()
            );
            for (RebCommercialPropertyType propertyType
                : RebCommercialPropertyType.values()) {
                for (RebCommercialMetricType metricType
                    : RebCommercialMetricType.values()) {
                    int pageNumber = 1;
                    int totalPages;
                    do {
                        Map<String, String> parameters = parameters(
                            propertyType,
                            metricType,
                            rebPeriod,
                            pageNumber
                        );
                        String response = apiClient.fetchStatistics(parameters);
                        RebCommercialRentPage parsed = parser.parse(
                            response,
                            propertyType,
                            metricType,
                            rebPeriod
                        );
                        RawApiPayloadEntity payload = rawPayloadService.save(
                            new RawPayloadCommand(
                                SOURCE_NAME,
                                SERVICE_NAME,
                                "/r-one/openapi/SttsApiTblData.do",
                                json(parameters),
                                response,
                                parsed.sourceRowCount(),
                                job.getId()
                            )
                        );
                        parsed.observations().forEach(observation ->
                            collected.add(
                                new CollectedRebCommercialRentObservation(
                                    payload,
                                    observation
                                )
                            )
                        );
                        pages++;
                        fetched += parsed.sourceRowCount();
                        normalized += parsed.observations().size();
                        totalPages = Math.max(
                            1,
                            (parsed.totalCount() + pageSize - 1) / pageSize
                        );
                        pageNumber++;
                    } while (pageNumber <= totalPages);
                }
            }
            int persisted = persistenceService.replaceReb(period, collected);
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

    private Map<String, String> parameters(
        RebCommercialPropertyType propertyType,
        RebCommercialMetricType metricType,
        String period,
        int pageNumber
    ) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("Type", "json");
        parameters.put("pIndex", String.valueOf(pageNumber));
        parameters.put("pSize", String.valueOf(pageSize));
        parameters.put("STATBL_ID", propertyType.tableId(metricType));
        parameters.put("DTACYCLE_CD", "QY");
        parameters.put("WRTTIME_IDTFR_ID", period);
        return parameters;
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("Request parameter JSON failed.", exception);
        }
    }
}
