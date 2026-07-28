package com.capstone.ai_insite.dataimport.service;

import com.capstone.ai_insite.dataimport.client.PublicDataApiClient;
import com.capstone.ai_insite.dataimport.domain.DataImportJobProgress;
import com.capstone.ai_insite.dataimport.domain.DataImportJobStatus;
import com.capstone.ai_insite.dataimport.domain.RawPayloadCommand;
import com.capstone.ai_insite.dataimport.domain.SmallBusinessStoreImportCommand;
import com.capstone.ai_insite.dataimport.domain.SmallBusinessStoreImportResult;
import com.capstone.ai_insite.dataimport.dto.publicdata.SmallBusinessStoreApiPage;
import com.capstone.ai_insite.dataimport.entity.DataImportJobEntity;
import com.capstone.ai_insite.dataimport.mapper.SmallBusinessStoreApiParser;
import com.capstone.ai_insite.dataimport.mapper.SmallBusinessStoreRowMapper;
import com.capstone.ai_insite.metric.service.CompetitionFeatureAggregationService;
import com.capstone.ai_insite.region.domain.SeoulDistrict;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@ConditionalOnProperty(name = "external.public-data.enabled", havingValue = "true")
public class SmallBusinessStoreDataImportService {

    public static final String SOURCE_NAME = "PUBLIC_DATA_PORTAL";
    public static final String SERVICE_PREFIX = "storeListInDong";

    private final PublicDataApiClient apiClient;
    private final SmallBusinessStoreApiParser parser;
    private final SmallBusinessStoreRowMapper rowMapper;
    private final RawPayloadService rawPayloadService;
    private final SmallBusinessStoreImportService importService;
    private final DataImportJobService jobService;
    private final CompetitionFeatureAggregationService aggregationService;
    private final ObjectMapper objectMapper;
    private final String apiPath;
    private final int pageSize;
    private final int maxPagesPerDistrict;

    public SmallBusinessStoreDataImportService(
        PublicDataApiClient apiClient,
        SmallBusinessStoreApiParser parser,
        SmallBusinessStoreRowMapper rowMapper,
        RawPayloadService rawPayloadService,
        SmallBusinessStoreImportService importService,
        DataImportJobService jobService,
        CompetitionFeatureAggregationService aggregationService,
        ObjectMapper objectMapper,
        @Value("${external.public-data.small-business.path}") String apiPath,
        @Value("${external.public-data.small-business.page-size}") int pageSize,
        @Value("${external.public-data.small-business.max-pages-per-district}")
        int maxPagesPerDistrict
    ) {
        if (pageSize < 1 || pageSize > 1000) {
            throw new IllegalArgumentException(
                "Small-business API page size must be between 1 and 1000."
            );
        }
        if (maxPagesPerDistrict < 1) {
            throw new IllegalArgumentException(
                "Small-business API max pages must be positive."
            );
        }
        this.apiClient = apiClient;
        this.parser = parser;
        this.rowMapper = rowMapper;
        this.rawPayloadService = rawPayloadService;
        this.importService = importService;
        this.jobService = jobService;
        this.aggregationService = aggregationService;
        this.objectMapper = objectMapper;
        this.apiPath = apiPath;
        this.pageSize = pageSize;
        this.maxPagesPerDistrict = maxPagesPerDistrict;
    }

    public SmallBusinessStoreImportResult collect(
        String districtCode,
        String requestedBy
    ) {
        List<SeoulDistrict> districts = districts(districtCode);
        SmallBusinessStoreApiPage discovery = fetch(
            districts.getFirst(),
            1,
            1
        ).page();
        return collect(
            districts,
            discovery.standardMonth(),
            requestedBy,
            null
        );
    }

    public SmallBusinessStoreImportResult retry(Long failedJobId) {
        DataImportJobEntity failedJob = jobService.get(failedJobId);
        if (failedJob.getStatus() != DataImportJobStatus.FAILED) {
            throw new IllegalArgumentException("Only failed import jobs can be retried.");
        }
        List<SeoulDistrict> districts = districtsFromServiceName(
            failedJob.getServiceName()
        );
        SmallBusinessStoreApiPage discovery = fetch(
            districts.getFirst(),
            1,
            1
        ).page();
        if (!failedJob.getTargetPeriod().equals(discovery.standardMonth())) {
            throw new IllegalStateException(
                "The API standard month changed after the failed job: "
                    + failedJob.getTargetPeriod() + " -> " + discovery.standardMonth()
            );
        }
        return collect(
            districts,
            failedJob.getTargetPeriod(),
            failedJob.getRequestedBy(),
            failedJobId
        );
    }

    private SmallBusinessStoreImportResult collect(
        List<SeoulDistrict> districts,
        String standardMonth,
        String requestedBy,
        Long retryOfJobId
    ) {
        String serviceName = serviceName(districts);
        DataImportJobEntity job = jobService.start(
            SOURCE_NAME,
            serviceName,
            standardMonth,
            requestedBy,
            retryOfJobId
        );
        MutableProgress progress = new MutableProgress();
        try {
            for (SeoulDistrict district : districts) {
                collectDistrict(district, standardMonth, job.getId(), progress);
            }
            LocalDate snapshotDate = rowMapper.parseMonth(standardMonth).atEndOfMonth();
            int competitionCount = aggregationService.aggregate(snapshotDate);
            DataImportJobProgress finalProgress = progress.toValue();
            jobService.complete(job.getId(), finalProgress);
            return progress.toResult(
                job.getId(),
                standardMonth,
                snapshotDate,
                competitionCount
            );
        } catch (RuntimeException exception) {
            jobService.fail(job.getId(), exception);
            throw exception;
        }
    }

    private void collectDistrict(
        SeoulDistrict district,
        String standardMonth,
        Long jobId,
        MutableProgress progress
    ) {
        for (int pageNumber = 1; pageNumber <= maxPagesPerDistrict; pageNumber++) {
            FetchedPage fetchedPage;
            try {
                fetchedPage = fetch(district, pageNumber, pageSize);
            } catch (RuntimeException exception) {
                throw new IllegalStateException(
                    "Failed to collect district " + district.code()
                        + ", page " + pageNumber,
                    exception
                );
            }
            SmallBusinessStoreApiPage page = fetchedPage.page();
            if (!standardMonth.equals(page.standardMonth())) {
                throw new IllegalStateException(
                    "The API standard month changed during collection: "
                        + standardMonth + " -> " + page.standardMonth()
                );
            }
            Map<String, String> requestParameters = requestParameters(
                district,
                pageNumber,
                pageSize
            );
            Long rawPayloadId = rawPayloadService.save(new RawPayloadCommand(
                SOURCE_NAME,
                SERVICE_PREFIX,
                apiPath,
                serialize(requestParameters),
                fetchedPage.responseBody(),
                page.rows().size(),
                jobId
            )).getId();

            List<SmallBusinessStoreImportCommand> commands = new ArrayList<>();
            int rejected = 0;
            for (var row : page.rows()) {
                try {
                    commands.add(rowMapper.toCommand(row, standardMonth));
                } catch (RuntimeException exception) {
                    rejected++;
                }
            }
            var batch = importService.importBatch(rawPayloadId, commands);
            progress.record(
                page.rows().size(),
                batch.normalizedRowCount(),
                rejected,
                batch.regionMappedRowCount(),
                batch.categoryMappedRowCount()
            );
            jobService.record(jobId, progress.toValue());
            if (page.rows().isEmpty()
                || (long) pageNumber * page.numberOfRows() >= page.totalCount()) {
                return;
            }
        }
        throw new IllegalStateException(
            "Small-business API exceeded max pages for district " + district.code()
        );
    }

    private FetchedPage fetch(
        SeoulDistrict district,
        int pageNumber,
        int requestedPageSize
    ) {
        String responseBody = apiClient.fetch(
            apiPath,
            requestParameters(district, pageNumber, requestedPageSize)
        );
        return new FetchedPage(parser.parse(responseBody), responseBody);
    }

    private static Map<String, String> requestParameters(
        SeoulDistrict district,
        int pageNumber,
        int requestedPageSize
    ) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("divId", "signguCd");
        parameters.put("key", district.code());
        parameters.put("numOfRows", Integer.toString(requestedPageSize));
        parameters.put("pageNo", Integer.toString(pageNumber));
        parameters.put("type", "json");
        return parameters;
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot serialize import metadata.", exception);
        }
    }

    private static List<SeoulDistrict> districts(String districtCode) {
        if (districtCode == null || districtCode.isBlank()) {
            return List.of(SeoulDistrict.values());
        }
        return Arrays.stream(SeoulDistrict.values())
            .filter(district -> district.code().equals(districtCode.trim()))
            .findFirst()
            .map(List::of)
            .orElseThrow(() -> new IllegalArgumentException(
                "Unsupported Seoul district code: " + districtCode
            ));
    }

    private static List<SeoulDistrict> districtsFromServiceName(String serviceName) {
        if ((SERVICE_PREFIX + ":SEOUL").equals(serviceName)) {
            return List.of(SeoulDistrict.values());
        }
        String prefix = SERVICE_PREFIX + ":";
        if (serviceName != null && serviceName.startsWith(prefix)) {
            return districts(serviceName.substring(prefix.length()));
        }
        throw new IllegalArgumentException(
            "The failed job is not a small-business store import: " + serviceName
        );
    }

    private static String serviceName(List<SeoulDistrict> districts) {
        return districts.size() == SeoulDistrict.values().length
            ? SERVICE_PREFIX + ":SEOUL"
            : SERVICE_PREFIX + ":" + districts.getFirst().code();
    }

    private record FetchedPage(
        SmallBusinessStoreApiPage page,
        String responseBody
    ) {
    }

    private static final class MutableProgress {
        private int pageCount;
        private long fetched;
        private long normalized;
        private long rejected;
        private long regionMapped;
        private long categoryMapped;

        void record(
            int fetchedRows,
            int normalizedRows,
            int rejectedRows,
            int regionMappedRows,
            int categoryMappedRows
        ) {
            pageCount++;
            fetched += fetchedRows;
            normalized += normalizedRows;
            rejected += rejectedRows;
            regionMapped += regionMappedRows;
            categoryMapped += categoryMappedRows;
        }

        DataImportJobProgress toValue() {
            return new DataImportJobProgress(
                pageCount,
                fetched,
                normalized,
                rejected
            );
        }

        SmallBusinessStoreImportResult toResult(
            Long jobId,
            String standardMonth,
            LocalDate snapshotDate,
            int competitionCount
        ) {
            return new SmallBusinessStoreImportResult(
                jobId,
                standardMonth,
                snapshotDate,
                pageCount,
                fetched,
                normalized,
                rejected,
                regionMapped,
                categoryMapped,
                competitionCount
            );
        }
    }
}
