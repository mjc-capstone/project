package com.capstone.ai_insite.dataimport.controller;

import com.capstone.ai_insite.dataimport.dto.CostDataImportResponse;
import com.capstone.ai_insite.dataimport.service.BuildingRegisterDataImportService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/import-jobs/public-data/building-registers")
@ConditionalOnProperty(name = "external.public-data.enabled", havingValue = "true")
public class BuildingRegisterImportController {

    private final BuildingRegisterDataImportService importService;

    public BuildingRegisterImportController(
        BuildingRegisterDataImportService importService
    ) {
        this.importService = importService;
    }

    @PostMapping
    public CostDataImportResponse collect(
        @RequestParam String period,
        @RequestParam(required = false) String districtCode,
        @RequestParam(required = false) String legalDongCode,
        @RequestParam(required = false) String requestedBy
    ) {
        return result(
            period,
            districtCode,
            legalDongCode,
            requestedBy,
            null
        );
    }

    @PostMapping("/jobs/{jobId}/retry")
    public CostDataImportResponse retry(
        @PathVariable Long jobId,
        @RequestParam String period,
        @RequestParam(required = false) String districtCode,
        @RequestParam(required = false) String legalDongCode,
        @RequestParam(required = false) String requestedBy
    ) {
        return result(
            period,
            districtCode,
            legalDongCode,
            requestedBy,
            jobId
        );
    }

    private CostDataImportResponse result(
        String period,
        String districtCode,
        String legalDongCode,
        String requestedBy,
        Long retryOfJobId
    ) {
        return CostDataImportResponse.from(importService.importSnapshot(
            period,
            districtCode,
            legalDongCode,
            requestedBy,
            retryOfJobId
        ));
    }
}
