package com.capstone.ai_insite.dataimport.service;

import com.capstone.ai_insite.dataimport.domain.SeoulCommercialHistoryImportResult;
import com.capstone.ai_insite.dataimport.domain.SeoulCommercialImportResult;
import com.capstone.ai_insite.dataimport.domain.SeoulQuarter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "external.seoul.enabled", havingValue = "true")
public class SeoulCommercialHistoryImportService {

    private static final int MAX_QUARTER_COUNT = 20;

    private final SeoulCommercialDataImportService quarterImportService;

    public SeoulCommercialHistoryImportService(
        SeoulCommercialDataImportService quarterImportService
    ) {
        this.quarterImportService = quarterImportService;
    }

    public SeoulCommercialHistoryImportResult importRange(
        String fromSourcePeriod,
        String toSourcePeriod
    ) {
        List<SeoulQuarter> quarters = SeoulQuarter.rangeInclusive(
            fromSourcePeriod,
            toSourcePeriod,
            MAX_QUARTER_COUNT
        );
        List<SeoulCommercialImportResult> results = new ArrayList<>(quarters.size());
        for (SeoulQuarter quarter : quarters) {
            results.add(quarterImportService.importQuarter(quarter.sourceCode()));
        }
        return new SeoulCommercialHistoryImportResult(
            fromSourcePeriod,
            toSourcePeriod,
            results.size(),
            results.stream().mapToInt(SeoulCommercialImportResult::salesRowCount).sum(),
            results.stream().mapToInt(SeoulCommercialImportResult::storesRowCount).sum(),
            results.stream().mapToInt(
                SeoulCommercialImportResult::metricSnapshotCount
            ).sum(),
            List.copyOf(results)
        );
    }
}
