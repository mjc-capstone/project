package com.capstone.ai_insite.metric.controller;

import com.capstone.ai_insite.metric.dto.NearbyCompetitionResponse;
import com.capstone.ai_insite.metric.service.NearbyCompetitionQueryService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics/competition")
public class NearbyCompetitionController {

    private final NearbyCompetitionQueryService queryService;

    public NearbyCompetitionController(
        NearbyCompetitionQueryService queryService
    ) {
        this.queryService = queryService;
    }

    @GetMapping("/nearby")
    public NearbyCompetitionResponse nearby(
        @RequestParam BigDecimal latitude,
        @RequestParam BigDecimal longitude,
        @RequestParam Long businessCategoryId,
        @RequestParam int radiusMeters,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate snapshotDate
    ) {
        return queryService.find(
            latitude,
            longitude,
            businessCategoryId,
            radiusMeters,
            snapshotDate
        );
    }
}
