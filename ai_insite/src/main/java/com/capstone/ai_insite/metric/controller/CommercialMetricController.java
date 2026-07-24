package com.capstone.ai_insite.metric.controller;

import com.capstone.ai_insite.metric.dto.CommercialMetricResponse;
import com.capstone.ai_insite.metric.service.CommercialMetricQueryService;
import com.capstone.ai_insite.metric.service.MetricAggregationService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
public class CommercialMetricController {

    private final CommercialMetricQueryService queryService;
    private final MetricAggregationService aggregationService;

    public CommercialMetricController(
        CommercialMetricQueryService queryService,
        MetricAggregationService aggregationService
    ) {
        this.queryService = queryService;
        this.aggregationService = aggregationService;
    }

    @GetMapping("/summary")
    public CommercialMetricResponse summary(
        @RequestParam String regionCode,
        @RequestParam String categoryCode,
        @RequestParam String period
    ) {
        return CommercialMetricResponse.from(
            queryService.getSummary(regionCode, categoryCode, period)
        );
    }

    @GetMapping("/trends")
    public List<CommercialMetricResponse> trends(
        @RequestParam String regionCode,
        @RequestParam String categoryCode,
        @RequestParam String from,
        @RequestParam String to
    ) {
        return queryService.getTrends(regionCode, categoryCode, from, to).stream()
            .map(CommercialMetricResponse::from)
            .toList();
    }

    @PostMapping("/aggregate")
    public CommercialMetricResponse aggregate(
        @RequestParam String regionCode,
        @RequestParam String categoryCode,
        @RequestParam String period
    ) {
        return CommercialMetricResponse.from(
            aggregationService.aggregate(regionCode, categoryCode, period)
        );
    }
}
