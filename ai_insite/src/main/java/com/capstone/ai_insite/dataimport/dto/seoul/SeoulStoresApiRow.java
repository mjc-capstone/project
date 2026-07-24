package com.capstone.ai_insite.dataimport.dto.seoul;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class SeoulStoresApiRow {

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

    @JsonProperty("SIMILR_INDUTY_STOR_CO")
    private BigDecimal similarIndustryStoreCount;

    @JsonProperty("STOR_CO")
    private BigDecimal normalStoreCount;

    @JsonProperty("FRC_STOR_CO")
    private BigDecimal franchiseStoreCount;

    @JsonProperty("OPBIZ_RT")
    private BigDecimal openRate;

    @JsonProperty("OPBIZ_STOR_CO")
    private BigDecimal openStoreCount;

    @JsonProperty("CLSBIZ_RT")
    private BigDecimal closeRate;

    @JsonProperty("CLSBIZ_STOR_CO")
    private BigDecimal closeStoreCount;

    private final Map<String, Object> details = new LinkedHashMap<>();

    @JsonAnySetter
    public void addDetail(String fieldName, Object value) {
        details.put(fieldName, value);
    }
}
