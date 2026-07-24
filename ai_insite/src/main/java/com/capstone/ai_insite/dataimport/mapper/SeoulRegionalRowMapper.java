package com.capstone.ai_insite.dataimport.mapper;

import com.capstone.ai_insite.dataimport.domain.SeoulQuarter;
import com.capstone.ai_insite.dataimport.domain.SeoulRegionalImportCommand;
import com.capstone.ai_insite.dataimport.dto.seoul.SeoulApiRow;
import com.capstone.ai_insite.dataimport.dto.seoul.SeoulRegionalApiRow;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class SeoulRegionalRowMapper {

    private static final String[] AGES = {"10", "20", "30", "40", "50", "60_ABOVE"};
    private static final String[] DAYS = {"MON", "TUES", "WED", "THUR", "FRI", "SAT", "SUN"};
    private static final String[] TIMES = {"00_06", "06_11", "11_14", "14_17", "17_21", "21_24"};
    private static final String[] AREA_KEYS = {
        "AE_66_SQMT_BELO_HSHLD_CO", "AE_66_SQMT_HSHLD_CO",
        "AE_99_SQMT_HSHLD_CO", "AE_132_SQMT_HSHLD_CO", "AE_165_SQMT_HSHLD_CO"
    };
    private static final String[] PRICE_KEYS = {
        "PC_1_HDMIL_BELO_HSHLD_CO", "PC_1_HDMIL_HSHLD_CO",
        "PC_2_HDMIL_HSHLD_CO", "PC_3_HDMIL_HSHLD_CO",
        "PC_4_HDMIL_HSHLD_CO", "PC_5_HDMIL_HSHLD_CO",
        "PC_6_HDMIL_ABOVE_HSHLD_CO"
    };

    private final ObjectMapper objectMapper;

    public SeoulRegionalRowMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SeoulRegionalImportCommand.FloatingPopulation toFloatingPopulation(
        SeoulApiRow<SeoulRegionalApiRow> source
    ) {
        SeoulRegionalApiRow row = source.value();
        SeoulQuarter quarter = SeoulQuarter.parse(row.getSourcePeriodCode());
        return new SeoulRegionalImportCommand.FloatingPopulation(
            row.getRegionCode(), row.getRegionName(), quarter.periodCode(), quarter.sourceCode(),
            longValue(row.detail("TOT_FLPOP_CO")),
            serialize(values(row, "ML_FLPOP_CO", "FML_FLPOP_CO")),
            serialize(prefixedValues(row, "AGRDE_", "_FLPOP_CO", AGES)),
            serialize(suffixedValues(row, "_FLPOP_CO", DAYS)),
            serialize(prefixedValues(row, "TMZON_", "_FLPOP_CO", TIMES)),
            source.sourceJson()
        );
    }

    public SeoulRegionalImportCommand.ResidentPopulation toResidentPopulation(
        SeoulApiRow<SeoulRegionalApiRow> source
    ) {
        SeoulRegionalApiRow row = source.value();
        SeoulQuarter quarter = SeoulQuarter.parse(row.getSourcePeriodCode());
        return new SeoulRegionalImportCommand.ResidentPopulation(
            row.getRegionCode(), row.getRegionName(), quarter.periodCode(), quarter.sourceCode(),
            longValue(row.detail("TOT_REPOP_CO")),
            serialize(values(row, "ML_REPOP_CO", "FML_REPOP_CO")),
            serialize(prefixedValues(row, "AGRDE_", "_REPOP_CO", AGES)),
            longValue(row.detail("TOT_HSHLD_CO")),
            serialize(values(row, "APT_HSHLD_CO", "NON_APT_HSHLD_CO")),
            source.sourceJson()
        );
    }

    public SeoulRegionalImportCommand.WorkingPopulation toWorkingPopulation(
        SeoulApiRow<SeoulRegionalApiRow> source
    ) {
        SeoulRegionalApiRow row = source.value();
        SeoulQuarter quarter = SeoulQuarter.parse(row.getSourcePeriodCode());
        return new SeoulRegionalImportCommand.WorkingPopulation(
            row.getRegionCode(), row.getRegionName(), quarter.periodCode(), quarter.sourceCode(),
            longValue(row.detail("TOT_WRC_POPLTN_CO")),
            longValue(row.detail("ML_WRC_POPLTN_CO")),
            longValue(row.detail("FML_WRC_POPLTN_CO")),
            serialize(prefixedValues(row, "AGRDE_", "_WRC_POPLTN_CO", AGES)),
            serialize(prefixedValues(row, "MAG_", "_WRC_POPLTN_CO", AGES)),
            serialize(prefixedValues(row, "FAG_", "_WRC_POPLTN_CO", AGES)),
            source.sourceJson()
        );
    }

    public SeoulRegionalImportCommand.Facilities toFacilities(
        SeoulApiRow<SeoulRegionalApiRow> source
    ) {
        SeoulRegionalApiRow row = source.value();
        SeoulQuarter quarter = SeoulQuarter.parse(row.getSourcePeriodCode());
        return new SeoulRegionalImportCommand.Facilities(
            row.getRegionCode(), row.getRegionName(), quarter.periodCode(), quarter.sourceCode(),
            intValue(row.detail("VIATR_FCLTY_CO")), intValue(row.detail("PBLOFC_CO")),
            intValue(row.detail("BANK_CO")), intValue(row.detail("GEHSPT_CO")),
            intValue(row.detail("GNRL_HSPTL_CO")), intValue(row.detail("PARMACY_CO")),
            intValue(row.detail("KNDRGR_CO")), intValue(row.detail("ELESCH_CO")),
            intValue(row.detail("MSKUL_CO")), intValue(row.detail("HGSCHL_CO")),
            intValue(row.detail("UNIV_CO")), intValue(row.detail("DRTS_CO")),
            intValue(row.detail("SUPMK_CO")), intValue(row.detail("THEAT_CO")),
            intValue(row.detail("STAYNG_FCLTY_CO")), intValue(row.detail("RLROAD_STATN_CO")),
            intValue(row.detail("SUBWAY_STATN_CO")), intValue(row.detail("BUS_STTN_CO")),
            serialize(row.getDetails()), source.sourceJson()
        );
    }

    public SeoulRegionalImportCommand.Apartments toApartments(
        SeoulApiRow<SeoulRegionalApiRow> source
    ) {
        SeoulRegionalApiRow row = source.value();
        SeoulQuarter quarter = SeoulQuarter.parse(row.getSourcePeriodCode());
        return new SeoulRegionalImportCommand.Apartments(
            row.getRegionCode(), row.getRegionName(), quarter.periodCode(), quarter.sourceCode(),
            intValue(row.detail("APT_HSMP_CO")),
            serialize(values(row, AREA_KEYS)), serialize(values(row, PRICE_KEYS)),
            decimalValue(row.detail("AVRG_AE")), longValue(row.detail("AVRG_MKTC")),
            source.sourceJson()
        );
    }

    private static Map<String, Object> prefixedValues(
        SeoulRegionalApiRow row,
        String prefix,
        String suffix,
        String[] keys
    ) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String key : keys) {
            result.put(key, row.detail(prefix + key + suffix));
        }
        return result;
    }

    private static Map<String, Object> suffixedValues(
        SeoulRegionalApiRow row,
        String suffix,
        String[] keys
    ) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String key : keys) {
            result.put(key, row.detail(key + suffix));
        }
        return result;
    }

    private static Map<String, Object> values(SeoulRegionalApiRow row, String... keys) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String key : keys) {
            result.put(key, row.detail(key));
        }
        return result;
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("서울시 지역 세부 JSON 생성에 실패했습니다.", exception);
        }
    }

    private static BigDecimal decimalValue(Object value) {
        return value == null ? null : new BigDecimal(value.toString());
    }

    private static Long longValue(Object value) {
        BigDecimal decimal = decimalValue(value);
        return decimal == null ? null : decimal.longValue();
    }

    private static Integer intValue(Object value) {
        BigDecimal decimal = decimalValue(value);
        return decimal == null ? null : decimal.intValue();
    }
}
