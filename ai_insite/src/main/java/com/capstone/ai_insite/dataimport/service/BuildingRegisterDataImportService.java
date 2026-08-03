package com.capstone.ai_insite.dataimport.service;

import com.capstone.ai_insite.dataimport.client.PublicDataApiClient;
import com.capstone.ai_insite.dataimport.domain.CostDataImportResult;
import com.capstone.ai_insite.dataimport.domain.DataImportJobProgress;
import com.capstone.ai_insite.dataimport.domain.RawPayloadCommand;
import com.capstone.ai_insite.dataimport.domain.SeoulQuarter;
import com.capstone.ai_insite.dataimport.dto.publicdata.BuildingRegisterPage;
import com.capstone.ai_insite.dataimport.dto.publicdata.CollectedBuildingRegister;
import com.capstone.ai_insite.dataimport.entity.DataImportJobEntity;
import com.capstone.ai_insite.dataimport.entity.RawApiPayloadEntity;
import com.capstone.ai_insite.dataimport.mapper.BuildingRegisterApiParser;
import com.capstone.ai_insite.metric.entity.MetricPeriodEntity;
import com.capstone.ai_insite.metric.repository.MetricPeriodJpaRepository;
import com.capstone.ai_insite.metric.service.BuiltEnvironmentFeatureAggregationService;
import com.capstone.ai_insite.region.entity.LegalDongEntity;
import com.capstone.ai_insite.region.repository.LegalDongJpaRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@ConditionalOnProperty(name = "external.public-data.enabled", havingValue = "true")
public class BuildingRegisterDataImportService {

    private static final String SOURCE_NAME = "MOLIT_BUILDING_HUB";
    private static final String SERVICE_NAME = "buildingRegisterTitle";

    private final PublicDataApiClient apiClient;
    private final BuildingRegisterApiParser parser;
    private final DataImportJobService jobService;
    private final RawPayloadService rawPayloadService;
    private final BuildingRegisterPersistenceService persistenceService;
    private final BuiltEnvironmentFeatureAggregationService aggregationService;
    private final LegalDongJpaRepository legalDongRepository;
    private final MetricPeriodJpaRepository periodRepository;
    private final ObjectMapper objectMapper;
    private final String path;
    private final int pageSize;
    private final int maxPagesPerLegalDong;

    public BuildingRegisterDataImportService(
        PublicDataApiClient apiClient,
        BuildingRegisterApiParser parser,
        DataImportJobService jobService,
        RawPayloadService rawPayloadService,
        BuildingRegisterPersistenceService persistenceService,
        BuiltEnvironmentFeatureAggregationService aggregationService,
        LegalDongJpaRepository legalDongRepository,
        MetricPeriodJpaRepository periodRepository,
        ObjectMapper objectMapper,
        @Value("${external.public-data.building-register.path}") String path,
        @Value("${external.public-data.building-register.page-size}") int pageSize,
        @Value("${external.public-data.building-register.max-pages-per-legal-dong}")
        int maxPagesPerLegalDong
    ) {
        this.apiClient = apiClient;
        this.parser = parser;
        this.jobService = jobService;
        this.rawPayloadService = rawPayloadService;
        this.persistenceService = persistenceService;
        this.aggregationService = aggregationService;
        this.legalDongRepository = legalDongRepository;
        this.periodRepository = periodRepository;
        this.objectMapper = objectMapper;
        this.path = path;
        this.pageSize = pageSize;
        this.maxPagesPerLegalDong = maxPagesPerLegalDong;
    }

    public CostDataImportResult importSnapshot(
        String sourcePeriodCode,
        String districtCode,
        String legalDongCode,
        String requestedBy,
        Long retryOfJobId
    ) {
        LocalDate snapshotDate = LocalDate.now();
        SeoulQuarter quarter = SeoulQuarter.parse(sourcePeriodCode);
        MetricPeriodEntity period = periodRepository
            .findByPeriodCode(quarter.periodCode())
            .orElseGet(() -> periodRepository.save(
                MetricPeriodEntity.createQuarter(
                    quarter.periodCode(),
                    quarter.year(),
                    quarter.quarter()
                )
            ));
        validateSnapshotDate(period, snapshotDate);
        List<LegalDongEntity> targets = targets(districtCode, legalDongCode);
        String jobTarget = jobTarget(snapshotDate, districtCode, legalDongCode);
        DataImportJobEntity job = jobService.start(
            SOURCE_NAME,
            SERVICE_NAME,
            jobTarget,
            requestedBy,
            retryOfJobId
        );
        int pages = 0;
        int fetched = 0;
        int normalized = 0;
        try {
            for (LegalDongEntity target : targets) {
                List<CollectedBuildingRegister> collected = new ArrayList<>();
                int pageNumber = 1;
                int totalPages;
                do {
                    Map<String, String> parameters = parameters(target, pageNumber);
                    String response = apiClient.fetch(path, parameters);
                    BuildingRegisterPage parsed = parser.parse(response);
                    RawApiPayloadEntity payload = rawPayloadService.save(
                        new RawPayloadCommand(
                            SOURCE_NAME,
                            SERVICE_NAME,
                            path,
                            json(parameters),
                            json(Map.of("json", response)),
                            parsed.rows().size(),
                            job.getId()
                        )
                    );
                    parsed.rows().forEach(row -> collected.add(
                        new CollectedBuildingRegister(payload, row)
                    ));
                    pages++;
                    fetched += parsed.rows().size();
                    int effectivePageSize = parsed.numberOfRows() > 0
                        ? parsed.numberOfRows()
                        : pageSize;
                    totalPages = Math.max(
                        1,
                        (parsed.totalCount() + effectivePageSize - 1)
                            / effectivePageSize
                    );
                    if (totalPages > maxPagesPerLegalDong) {
                        throw new IllegalStateException(
                            "Building register page limit exceeded for "
                                + target.getLegalDongCode() + ": " + totalPages
                        );
                    }
                    pageNumber++;
                } while (pageNumber <= totalPages);
                normalized += persistenceService.replace(
                    target,
                    snapshotDate,
                    collected
                );
                jobService.record(job.getId(), new DataImportJobProgress(
                    pages,
                    fetched,
                    normalized,
                    fetched - normalized
                ));
            }
            int generated = aggregationService.rebuild(period, snapshotDate);
            DataImportJobProgress progress = new DataImportJobProgress(
                pages,
                fetched,
                normalized,
                fetched - normalized
            );
            jobService.complete(job.getId(), progress);
            return new CostDataImportResult(
                job.getId(),
                period.getPeriodCode(),
                pages,
                fetched,
                normalized,
                fetched - normalized,
                generated
            );
        } catch (RuntimeException exception) {
            jobService.fail(job.getId(), exception);
            throw exception;
        }
    }

    private List<LegalDongEntity> targets(
        String districtCode,
        String legalDongCode
    ) {
        if (legalDongCode != null && !legalDongCode.isBlank()) {
            if (!legalDongCode.matches("11\\d{8}")) {
                throw new IllegalArgumentException(
                    "서울 법정동 코드는 10자리여야 합니다."
                );
            }
            LegalDongEntity legalDong = legalDongRepository
                .findByLegalDongCodeAndActiveTrue(legalDongCode)
                .orElseThrow(() -> new IllegalArgumentException(
                    "활성 법정동을 찾을 수 없습니다: " + legalDongCode
                ));
            if (districtCode != null && !districtCode.isBlank()
                && !districtCode.equals(legalDong.getSigunguCode())) {
                throw new IllegalArgumentException(
                    "법정동 코드가 요청한 자치구에 속하지 않습니다."
                );
            }
            return List.of(legalDong);
        }
        List<LegalDongEntity> values;
        if (districtCode != null && !districtCode.isBlank()) {
            if (!districtCode.matches("11\\d{3}")) {
                throw new IllegalArgumentException(
                    "서울 자치구 코드는 5자리여야 합니다."
                );
            }
            values = legalDongRepository.findBySigunguCodeAndActiveTrue(districtCode);
        } else {
            values = legalDongRepository.findByActiveTrue().stream()
                .filter(value -> value.getLegalDongCode().matches("11\\d{8}"))
                .toList();
        }
        if (values.isEmpty()) {
            throw new IllegalArgumentException("수집할 활성 서울 법정동이 없습니다.");
        }
        return values.stream()
            .sorted(Comparator.comparing(LegalDongEntity::getLegalDongCode))
            .toList();
    }

    private Map<String, String> parameters(
        LegalDongEntity legalDong,
        int pageNumber
    ) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("sigunguCd", legalDong.getLegalDongCode().substring(0, 5));
        parameters.put("bjdongCd", legalDong.getLegalDongCode().substring(5));
        parameters.put("numOfRows", String.valueOf(pageSize));
        parameters.put("pageNo", String.valueOf(pageNumber));
        parameters.put("_type", "json");
        return parameters;
    }

    private static String jobTarget(
        LocalDate snapshotDate,
        String districtCode,
        String legalDongCode
    ) {
        String scope = legalDongCode != null && !legalDongCode.isBlank()
            ? legalDongCode
            : districtCode != null && !districtCode.isBlank()
                ? districtCode
                : "SEOUL";
        return snapshotDate.toString().replace("-", "") + ":" + scope;
    }

    private static void validateSnapshotDate(
        MetricPeriodEntity period,
        LocalDate snapshotDate
    ) {
        if (snapshotDate == null) {
            throw new IllegalArgumentException("건축물 스냅샷 날짜는 필수입니다.");
        }
        if (snapshotDate.isBefore(period.getStartDate())
            || snapshotDate.isAfter(period.getEndDate())) {
            throw new IllegalArgumentException(
                "건축물 스냅샷 날짜는 대상 분기에 포함되어야 합니다."
            );
        }
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("JSON serialization failed.", exception);
        }
    }
}
