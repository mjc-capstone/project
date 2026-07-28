package com.capstone.ai_insite.dataimport.controller;

import com.capstone.ai_insite.dataimport.domain.DataImportJobStatus;
import com.capstone.ai_insite.dataimport.dto.DataImportJobResponse;
import com.capstone.ai_insite.dataimport.service.DataImportJobQueryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/import-jobs")
public class DataImportJobController {

    private final DataImportJobQueryService queryService;

    public DataImportJobController(DataImportJobQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    public List<DataImportJobResponse> list(
        @RequestParam(required = false) DataImportJobStatus status,
        @RequestParam(defaultValue = "50") int limit
    ) {
        return queryService.list(status, limit);
    }

    @GetMapping("/{jobId}")
    public DataImportJobResponse get(@PathVariable Long jobId) {
        return queryService.get(jobId);
    }
}
