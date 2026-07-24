package com.capstone.ai_insite.dataimport.mapper;

import com.capstone.ai_insite.dataimport.domain.SalesImportCommand;
import com.capstone.ai_insite.dataimport.domain.SeoulQuarter;
import com.capstone.ai_insite.dataimport.dto.seoul.SeoulApiRow;
import com.capstone.ai_insite.dataimport.dto.seoul.SeoulSalesApiRow;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class SeoulSalesRowMapper {

    private static final String[] DAYS = {
        "MON", "TUES", "WED", "THUR", "FRI", "SAT", "SUN"
    };
    private static final String[] TIMES = {
        "00_06", "06_11", "11_14", "14_17", "17_21", "21_24"
    };
    private static final String[] AGES = {
        "10", "20", "30", "40", "50", "60_ABOVE"
    };

    private final ObjectMapper objectMapper;

    public SeoulSalesRowMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SalesImportCommand toCommand(SeoulApiRow<SeoulSalesApiRow> source) {
        SeoulSalesApiRow row = source.value();
        SeoulQuarter quarter = SeoulQuarter.parse(row.getSourcePeriodCode());
        return new SalesImportCommand(
            null,
            row.getRegionCode(),
            row.getRegionName(),
            row.getCategoryCode(),
            row.getCategoryName(),
            quarter.periodCode(),
            quarter.sourceCode(),
            longValue(row.getSalesAmount()),
            longValue(row.getSalesCount()),
            longValue(row.getWeekdaySalesAmount()),
            longValue(row.getWeekendSalesAmount()),
            serialize(group(row, "", DAYS)),
            serialize(group(row, "TMZON_", TIMES)),
            serialize(demographic(row)),
            source.sourceJson()
        );
    }

    private static Map<String, Object> group(
        SeoulSalesApiRow row,
        String prefix,
        String[] keys
    ) {
        Map<String, Object> amount = new LinkedHashMap<>();
        Map<String, Object> count = new LinkedHashMap<>();
        for (String key : keys) {
            amount.put(key, row.detail(prefix + key + "_SELNG_AMT"));
            count.put(key, row.detail(prefix + key + "_SELNG_CO"));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("amount", amount);
        result.put("count", count);
        return result;
    }

    private static Map<String, Object> demographic(SeoulSalesApiRow row) {
        Map<String, Object> gender = new LinkedHashMap<>();
        gender.put("maleAmount", row.detail("ML_SELNG_AMT"));
        gender.put("femaleAmount", row.detail("FML_SELNG_AMT"));
        gender.put("maleCount", row.detail("ML_SELNG_CO"));
        gender.put("femaleCount", row.detail("FML_SELNG_CO"));

        Map<String, Object> ageAmount = new LinkedHashMap<>();
        Map<String, Object> ageCount = new LinkedHashMap<>();
        for (String age : AGES) {
            ageAmount.put(age, row.detail("AGRDE_" + age + "_SELNG_AMT"));
            ageCount.put(age, row.detail("AGRDE_" + age + "_SELNG_CO"));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("gender", gender);
        result.put("ageAmount", ageAmount);
        result.put("ageCount", ageCount);
        return result;
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("서울시 매출 세부 JSON 생성에 실패했습니다.", exception);
        }
    }

    private static Long longValue(java.math.BigDecimal value) {
        return value == null ? null : value.longValue();
    }
}
