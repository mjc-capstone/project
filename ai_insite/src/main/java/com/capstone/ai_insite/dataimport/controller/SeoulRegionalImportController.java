package com.capstone.ai_insite.dataimport.controller;

import com.capstone.ai_insite.dataimport.dto.SeoulRegionalImportResponse;
import com.capstone.ai_insite.dataimport.service.SeoulRegionalDataImportService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/import-jobs/seoul")
@ConditionalOnProperty(name = "external.seoul.enabled", havingValue = "true")
public class SeoulRegionalImportController {

    private final SeoulRegionalDataImportService importService;

    public SeoulRegionalImportController(SeoulRegionalDataImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/regional-features")
    public SeoulRegionalImportResponse importRegionalQuarter(
        @RequestParam String sourcePeriod
    ) {
        return SeoulRegionalImportResponse.from(
            importService.importQuarter(sourcePeriod)
        );
    }
}
