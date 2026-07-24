package com.capstone.ai_insite.dataimport.dto.seoul;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class SeoulSalesApiRow {

    @JsonProperty("STDR_YYQU_CD")
    private String sourcePeriodCode;

    @JsonProperty("ADSTRD_CD")
    private String regionCode;

    @JsonProperty("ADSTRD_CD_NM")
    private String regionName;

    @JsonProperty("SVC_INDUTY_CD")
    private String categoryCode;

    @JsonProperty("SVC_INDUTY_CD_NM")
    private String categoryName;

    @JsonProperty("THSMON_SELNG_AMT")
    private BigDecimal salesAmount;

    @JsonProperty("THSMON_SELNG_CO")
    private BigDecimal salesCount;

    @JsonProperty("MDWK_SELNG_AMT")
    private BigDecimal weekdaySalesAmount;

    @JsonProperty("WKEND_SELNG_AMT")
    private BigDecimal weekendSalesAmount;

    private final Map<String, Object> details = new LinkedHashMap<>();

    @JsonAnySetter
    public void addDetail(String fieldName, Object value) {
        details.put(fieldName, value);
    }

    public Object detail(String fieldName) {
        return details.get(fieldName);
    }
}
