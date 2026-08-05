package com.capstone.ai_insite.dataimport.controller;

import com.capstone.ai_insite.dataimport.dto.CostHistoryImportResponse;
import com.capstone.ai_insite.dataimport.service.CostHistoryDataImportService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/import-jobs/costs")
@ConditionalOnProperty(
    name = {"external.reb.enabled", "external.public-data.enabled"},
    havingValue = "true"
)
public class CostHistoryImportController {

    private final CostHistoryDataImportService importService;

    public CostHistoryImportController(CostHistoryDataImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/history")
    public CostHistoryImportResponse importHistory(
        @RequestParam String fromSourcePeriod,
        @RequestParam String toSourcePeriod,
        @RequestParam(required = false) String requestedBy
    ) {
        return CostHistoryImportResponse.from(importService.importRange(
            fromSourcePeriod,
            toSourcePeriod,
            requestedBy
        ));
    }
}
