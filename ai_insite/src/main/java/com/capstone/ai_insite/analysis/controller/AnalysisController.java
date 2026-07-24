package com.capstone.ai_insite.analysis.controller;

import com.capstone.ai_insite.analysis.dto.AnalysisCreateRequest;
import com.capstone.ai_insite.analysis.dto.AnalysisResultResponse;
import com.capstone.ai_insite.analysis.service.AnalysisApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AnalysisApplicationService analysisService;

    public AnalysisController(AnalysisApplicationService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnalysisResultResponse analyze(@RequestBody AnalysisCreateRequest request) {
        return AnalysisResultResponse.from(analysisService.analyze(request.toCommand()));
    }

    @GetMapping("/{analysisId}")
    public AnalysisResultResponse get(@PathVariable Long analysisId) {
        return AnalysisResultResponse.from(analysisService.get(analysisId));
    }
}
