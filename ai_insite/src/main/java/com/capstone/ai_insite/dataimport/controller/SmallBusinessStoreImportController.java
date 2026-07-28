package com.capstone.ai_insite.dataimport.controller;

import com.capstone.ai_insite.dataimport.domain.UnmappedCodeType;
import com.capstone.ai_insite.dataimport.dto.SmallBusinessStoreImportResponse;
import com.capstone.ai_insite.dataimport.dto.UnmappedCodeResponse;
import com.capstone.ai_insite.dataimport.service.SmallBusinessStoreDataImportService;
import com.capstone.ai_insite.dataimport.service.UnmappedStoreCodeQueryService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/import-jobs/public-data/small-business-stores")
@ConditionalOnProperty(name = "external.public-data.enabled", havingValue = "true")
public class SmallBusinessStoreImportController {

    private final SmallBusinessStoreDataImportService importService;
    private final UnmappedStoreCodeQueryService unmappedCodeQueryService;

    public SmallBusinessStoreImportController(
        SmallBusinessStoreDataImportService importService,
        UnmappedStoreCodeQueryService unmappedCodeQueryService
    ) {
        this.importService = importService;
        this.unmappedCodeQueryService = unmappedCodeQueryService;
    }

    @PostMapping
    public SmallBusinessStoreImportResponse collect(
        @RequestParam(required = false) String districtCode,
        @RequestParam(required = false) String requestedBy
    ) {
        return SmallBusinessStoreImportResponse.from(
            importService.collect(districtCode, requestedBy)
        );
    }

    @PostMapping("/jobs/{jobId}/retry")
    public SmallBusinessStoreImportResponse retry(@PathVariable Long jobId) {
        return SmallBusinessStoreImportResponse.from(importService.retry(jobId));
    }

    @GetMapping("/unmapped")
    public List<UnmappedCodeResponse> unmapped(
        @RequestParam UnmappedCodeType type,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate snapshotDate
    ) {
        return unmappedCodeQueryService.find(type, snapshotDate);
    }
}
