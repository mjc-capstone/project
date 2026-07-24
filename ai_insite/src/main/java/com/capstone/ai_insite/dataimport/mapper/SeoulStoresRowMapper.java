package com.capstone.ai_insite.dataimport.mapper;

import com.capstone.ai_insite.dataimport.domain.SeoulQuarter;
import com.capstone.ai_insite.dataimport.domain.StoreImportCommand;
import com.capstone.ai_insite.dataimport.dto.seoul.SeoulApiRow;
import com.capstone.ai_insite.dataimport.dto.seoul.SeoulStoresApiRow;
import org.springframework.stereotype.Component;

@Component
public class SeoulStoresRowMapper {

    public StoreImportCommand toCommand(SeoulApiRow<SeoulStoresApiRow> source) {
        SeoulStoresApiRow row = source.value();
        SeoulQuarter quarter = SeoulQuarter.parse(row.getSourcePeriodCode());
        return new StoreImportCommand(
            null,
            row.getRegionCode(),
            row.getRegionName(),
            row.getCategoryCode(),
            row.getCategoryName(),
            quarter.periodCode(),
            quarter.sourceCode(),
            intValue(row.getSimilarIndustryStoreCount()),
            intValue(row.getNormalStoreCount()),
            intValue(row.getFranchiseStoreCount()),
            row.getOpenRate(),
            intValue(row.getOpenStoreCount()),
            row.getCloseRate(),
            intValue(row.getCloseStoreCount()),
            source.sourceJson()
        );
    }

    private static Integer intValue(java.math.BigDecimal value) {
        return value == null ? null : value.intValue();
    }
}
