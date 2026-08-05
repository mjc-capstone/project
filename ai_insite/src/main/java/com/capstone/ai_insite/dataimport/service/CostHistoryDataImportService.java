package com.capstone.ai_insite.dataimport.service;

import com.capstone.ai_insite.dataimport.domain.CostHistoryImportResult;
import com.capstone.ai_insite.dataimport.domain.CostHistoryQuarterResult;
import com.capstone.ai_insite.dataimport.domain.SeoulQuarter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
    name = {"external.reb.enabled", "external.public-data.enabled"},
    havingValue = "true"
)
public class CostHistoryDataImportService {

    private static final int MAX_QUARTER_COUNT = 20;

    private final RebCommercialRentDataImportService rebImportService;
    private final CommercialTransactionDataImportService transactionImportService;

    public CostHistoryDataImportService(
        RebCommercialRentDataImportService rebImportService,
        CommercialTransactionDataImportService transactionImportService
    ) {
        this.rebImportService = rebImportService;
        this.transactionImportService = transactionImportService;
    }

    public CostHistoryImportResult importRange(
        String fromSourcePeriod,
        String toSourcePeriod,
        String requestedBy
    ) {
        List<SeoulQuarter> quarters = SeoulQuarter.rangeInclusive(
            fromSourcePeriod,
            toSourcePeriod,
            MAX_QUARTER_COUNT
        );
        List<CostHistoryQuarterResult> results = new ArrayList<>();
        for (SeoulQuarter quarter : quarters) {
            var reb = rebImportService.importQuarter(
                quarter.sourceCode(),
                requestedBy,
                null
            );
            var transactions = transactionImportService.importQuarter(
                quarter.sourceCode(),
                requestedBy,
                null
            );
            results.add(new CostHistoryQuarterResult(
                quarter.sourceCode(),
                reb,
                transactions
            ));
        }
        return new CostHistoryImportResult(
            fromSourcePeriod,
            toSourcePeriod,
            results.size(),
            results.stream().mapToInt(value -> value.reb().normalizedRowCount()).sum(),
            results.stream().mapToInt(value -> value.transactions().normalizedRowCount()).sum(),
            results.stream().mapToInt(value ->
                value.reb().generatedFeatureCount()
                    + value.transactions().generatedFeatureCount()
            ).sum(),
            List.copyOf(results)
        );
    }
}
