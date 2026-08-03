package com.capstone.ai_insite.dataimport.controller;

import com.capstone.ai_insite.dataimport.dto.CostDataImportResponse;
import com.capstone.ai_insite.dataimport.service.RebCommercialRentDataImportService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/import-jobs/reb/commercial-rent")
@ConditionalOnProperty(name = "external.reb.enabled", havingValue = "true")
public class RebCommercialRentImportController {

    private final RebCommercialRentDataImportService importService;

    public RebCommercialRentImportController(
        RebCommercialRentDataImportService importService
    ) {
        this.importService = importService;
    }

    @PostMapping
    public CostDataImportResponse collect(
        @RequestParam String period,
        @RequestParam(required = false) String requestedBy
    ) {
        return CostDataImportResponse.from(
            importService.importQuarter(period, requestedBy, null)
        );
    }

    @PostMapping("/jobs/{jobId}/retry")
    public CostDataImportResponse retry(
        @PathVariable Long jobId,
        @RequestParam String period,
        @RequestParam(required = false) String requestedBy
    ) {
        return CostDataImportResponse.from(
            importService.importQuarter(period, requestedBy, jobId)
        );
    }
}
