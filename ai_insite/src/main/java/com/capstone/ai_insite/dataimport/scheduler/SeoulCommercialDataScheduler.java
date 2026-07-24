package com.capstone.ai_insite.dataimport.scheduler;

import com.capstone.ai_insite.dataimport.service.SeoulCommercialDataImportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = {
        "external.seoul.enabled",
        "external.seoul.scheduler-enabled"
    },
    havingValue = "true"
)
public class SeoulCommercialDataScheduler {

    private final SeoulCommercialDataImportService importService;
    private final String sourcePeriodCode;

    public SeoulCommercialDataScheduler(
        SeoulCommercialDataImportService importService,
        @Value("${external.seoul.target-period}") String sourcePeriodCode
    ) {
        this.importService = importService;
        this.sourcePeriodCode = sourcePeriodCode;
    }

    @Scheduled(cron = "${external.seoul.schedule-cron}")
    public void collectCommercialSources() {
        importService.importQuarter(sourcePeriodCode);
    }
}
