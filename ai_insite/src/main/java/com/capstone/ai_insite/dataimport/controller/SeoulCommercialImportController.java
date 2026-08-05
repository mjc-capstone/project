package com.capstone.ai_insite.dataimport.controller;

import com.capstone.ai_insite.dataimport.dto.SeoulCommercialImportResponse;
import com.capstone.ai_insite.dataimport.dto.SeoulCommercialHistoryImportResponse;
import com.capstone.ai_insite.dataimport.dto.SeoulRegionalHistoryImportResponse;
import com.capstone.ai_insite.dataimport.service.SeoulCommercialDataImportService;
import com.capstone.ai_insite.dataimport.service.SeoulCommercialHistoryImportService;
import com.capstone.ai_insite.dataimport.service.SeoulRegionalDataImportService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/import-jobs/seoul")
@ConditionalOnProperty(name = "external.seoul.enabled", havingValue = "true")
public class SeoulCommercialImportController {

    private final SeoulCommercialDataImportService importService;
    private final SeoulCommercialHistoryImportService historyImportService;
    private final SeoulRegionalDataImportService regionalImportService;

    public SeoulCommercialImportController(
        SeoulCommercialDataImportService importService,
        SeoulCommercialHistoryImportService historyImportService,
        SeoulRegionalDataImportService regionalImportService
    ) {
        this.importService = importService;
        this.historyImportService = historyImportService;
        this.regionalImportService = regionalImportService;
    }

    @PostMapping("/commercial")
    public SeoulCommercialImportResponse importCommercialQuarter(
        @RequestParam String sourcePeriod
    ) {
        return SeoulCommercialImportResponse.from(
            importService.importQuarter(sourcePeriod)
        );
    }

    @PostMapping("/commercial/history")
    public SeoulCommercialHistoryImportResponse importCommercialHistory(
        @RequestParam String fromSourcePeriod,
        @RequestParam String toSourcePeriod
    ) {
        return SeoulCommercialHistoryImportResponse.from(
            historyImportService.importRange(fromSourcePeriod, toSourcePeriod)
        );
    }

    @PostMapping("/regional/history")
    public SeoulRegionalHistoryImportResponse importRegionalHistory(
        @RequestParam String fromSourcePeriod,
        @RequestParam String toSourcePeriod
    ) {
        return SeoulRegionalHistoryImportResponse.from(
            regionalImportService.importRange(fromSourcePeriod, toSourcePeriod)
        );
    }
}
