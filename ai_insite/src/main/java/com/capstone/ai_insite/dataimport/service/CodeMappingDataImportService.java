package com.capstone.ai_insite.dataimport.service;

import com.capstone.ai_insite.category.domain.CategoryMappingRebuildResult;
import com.capstone.ai_insite.category.service.CategoryMappingReviewService;
import com.capstone.ai_insite.category.service.SmallBusinessCategorySynchronizationService;
import com.capstone.ai_insite.dataimport.client.PublicDataApiClient;
import com.capstone.ai_insite.dataimport.domain.DataImportJobProgress;
import com.capstone.ai_insite.dataimport.domain.RawPayloadCommand;
import com.capstone.ai_insite.dataimport.dto.CodeMappingSynchronizationResponse;
import com.capstone.ai_insite.dataimport.dto.publicdata.SmallBusinessCategoryRow;
import com.capstone.ai_insite.dataimport.dto.publicdata.StandardLegalDongRow;
import com.capstone.ai_insite.dataimport.mapper.SmallBusinessCategoryApiParser;
import com.capstone.ai_insite.dataimport.mapper.StandardLegalDongApiParser;
import com.capstone.ai_insite.region.domain.RegionMappingRebuildResult;
import com.capstone.ai_insite.region.service.AdministrativeLegalDongMappingSynchronizationService;
import com.capstone.ai_insite.region.service.LegalDongSynchronizationService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@ConditionalOnProperty(name = "external.public-data.enabled", havingValue = "true")
public class CodeMappingDataImportService {

    private static final String MOIS_SOURCE = "MOIS_STANDARD_CODE";
    private static final String MOIS_SERVICE = "StanReginCd:SEOUL";
    private static final String MOIS_PATH =
        "/1741000/StanReginCd/getStanReginCdList";
    private static final String CATEGORY_SOURCE = "PUBLIC_DATA_PORTAL";
    private static final String CATEGORY_SERVICE = "smallBusinessCategoryHierarchy";
    private static final String CATEGORY_BASE =
        "/B553077/api/open/sdsc2";

    private final PublicDataApiClient apiClient;
    private final StandardLegalDongApiParser legalDongParser;
    private final SmallBusinessCategoryApiParser categoryParser;
    private final RawPayloadService rawPayloadService;
    private final DataImportJobService jobService;
    private final LegalDongSynchronizationService legalDongService;
    private final SmallBusinessCategorySynchronizationService categoryService;
    private final AdministrativeLegalDongMappingSynchronizationService
        regionMappingService;
    private final CategoryMappingReviewService categoryMappingService;
    private final ObjectMapper objectMapper;

    public CodeMappingDataImportService(
        PublicDataApiClient apiClient,
        StandardLegalDongApiParser legalDongParser,
        SmallBusinessCategoryApiParser categoryParser,
        RawPayloadService rawPayloadService,
        DataImportJobService jobService,
        LegalDongSynchronizationService legalDongService,
        SmallBusinessCategorySynchronizationService categoryService,
        AdministrativeLegalDongMappingSynchronizationService regionMappingService,
        CategoryMappingReviewService categoryMappingService,
        ObjectMapper objectMapper
    ) {
        this.apiClient = apiClient;
        this.legalDongParser = legalDongParser;
        this.categoryParser = categoryParser;
        this.rawPayloadService = rawPayloadService;
        this.jobService = jobService;
        this.legalDongService = legalDongService;
        this.categoryService = categoryService;
        this.regionMappingService = regionMappingService;
        this.categoryMappingService = categoryMappingService;
        this.objectMapper = objectMapper;
    }

    public CodeMappingSynchronizationResponse synchronize(
        LocalDate storeSnapshotDate,
        String requestedBy
    ) {
        LegalImportResult legal = importLegalDongs(requestedBy);
        RegionMappingRebuildResult regions =
            regionMappingService.rebuild(storeSnapshotDate);
        CategoryImportResult categories = importCategories(requestedBy);
        CategoryMappingRebuildResult categoryMappings =
            categoryMappingService.rebuild(storeSnapshotDate);
        return new CodeMappingSynchronizationResponse(
            legal.jobId(),
            legal.rowCount(),
            categories.jobId(),
            categories.rowCount(),
            regions,
            categoryMappings
        );
    }

    private LegalImportResult importLegalDongs(String requestedBy) {
        String target = LocalDate.now().toString();
        var job = jobService.start(
            MOIS_SOURCE,
            MOIS_SERVICE,
            target,
            requestedBy,
            null
        );
        int pageCount = 0;
        int fetched = 0;
        int normalized = 0;
        List<StandardLegalDongRow> allRows = new ArrayList<>();
        try {
            int pageNumber = 1;
            int totalCount;
            do {
                Map<String, String> parameters = new LinkedHashMap<>();
                parameters.put("pageNo", Integer.toString(pageNumber));
                parameters.put("numOfRows", "1000");
                parameters.put("type", "json");
                parameters.put("locatadd_nm", "서울특별시");
                String response = apiClient.fetch(MOIS_PATH, parameters);
                var page = legalDongParser.parse(response);
                totalCount = page.totalCount();
                allRows.addAll(page.rows());
                rawPayloadService.save(new RawPayloadCommand(
                    MOIS_SOURCE,
                    MOIS_SERVICE,
                    MOIS_PATH,
                    serialize(parameters),
                    response,
                    page.sourceRowCount(),
                    job.getId()
                ));
                pageCount++;
                fetched += page.sourceRowCount();
                normalized += page.rows().size();
                jobService.record(job.getId(), new DataImportJobProgress(
                    pageCount,
                    fetched,
                    normalized,
                    fetched - normalized
                ));
                pageNumber++;
            } while ((long) (pageNumber - 1) * 1000 < totalCount);
            int synchronizedRows = legalDongService.synchronize(
                allRows,
                LocalDate.now()
            );
            jobService.complete(job.getId(), new DataImportJobProgress(
                pageCount,
                fetched,
                normalized,
                fetched - normalized
            ));
            return new LegalImportResult(job.getId(), synchronizedRows);
        } catch (RuntimeException exception) {
            jobService.fail(job.getId(), exception);
            throw exception;
        }
    }

    private CategoryImportResult importCategories(String requestedBy) {
        String target = LocalDate.now().toString();
        var job = jobService.start(
            CATEGORY_SOURCE,
            CATEGORY_SERVICE,
            target,
            requestedBy,
            null
        );
        int pageCount = 0;
        List<SmallBusinessCategoryRow> smallCategories = new ArrayList<>();
        try {
            FetchResult largeFetch = fetchCategory(
                "/largeUpjongList",
                Map.of("type", "json"),
                job.getId()
            );
            pageCount++;
            var largeCodes = categoryParser.parseCodes(
                largeFetch.responseBody(),
                "indsLclsCd",
                "indsLclsNm"
            );
            for (var large : largeCodes) {
                FetchResult middleFetch = fetchCategory(
                    "/middleUpjongList",
                    Map.of(
                        "type", "json",
                        "indsLclsCd", large.code()
                    ),
                    job.getId()
                );
                pageCount++;
                var middleCodes = categoryParser.parseCodes(
                    middleFetch.responseBody(),
                    "indsMclsCd",
                    "indsMclsNm"
                );
                for (var middle : middleCodes) {
                    FetchResult smallFetch = fetchCategory(
                        "/smallUpjongList",
                        Map.of(
                            "type", "json",
                            "indsLclsCd", large.code(),
                            "indsMclsCd", middle.code()
                        ),
                        job.getId()
                    );
                    pageCount++;
                    smallCategories.addAll(
                        categoryParser.parseSmallCategories(
                            smallFetch.responseBody()
                        )
                    );
                    jobService.record(job.getId(), new DataImportJobProgress(
                        pageCount,
                        smallCategories.size(),
                        smallCategories.size(),
                        0
                    ));
                }
            }
            int synchronizedRows = categoryService.synchronize(smallCategories);
            jobService.complete(job.getId(), new DataImportJobProgress(
                pageCount,
                smallCategories.size(),
                smallCategories.size(),
                0
            ));
            return new CategoryImportResult(job.getId(), synchronizedRows);
        } catch (RuntimeException exception) {
            jobService.fail(job.getId(), exception);
            throw exception;
        }
    }

    private FetchResult fetchCategory(
        String operation,
        Map<String, String> parameters,
        Long jobId
    ) {
        String path = CATEGORY_BASE + operation;
        String response = apiClient.fetch(path, parameters);
        int rows = countItems(response);
        rawPayloadService.save(new RawPayloadCommand(
            CATEGORY_SOURCE,
            operation.substring(1),
            path,
            serialize(parameters),
            response,
            rows,
            jobId
        ));
        return new FetchResult(response);
    }

    private int countItems(String response) {
        try {
            return objectMapper.readTree(response)
                .path("body").path("items").size();
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot count category API rows.", exception);
        }
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot serialize request metadata.", exception);
        }
    }

    private record LegalImportResult(Long jobId, int rowCount) {
    }

    private record CategoryImportResult(Long jobId, int rowCount) {
    }

    private record FetchResult(String responseBody) {
    }
}
