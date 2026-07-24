package com.capstone.ai_insite.dataimport.dto.seoul;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class SeoulRegionalApiRow {

    @JsonProperty("STDR_YYQU_CD")
    private String sourcePeriodCode;

    @JsonProperty("ADSTRD_CD")
    private String regionCode;

    @JsonProperty("ADSTRD_CD_NM")
    private String regionName;

    private final Map<String, Object> details = new LinkedHashMap<>();

    @JsonAnySetter
    public void addDetail(String fieldName, Object value) {
        details.put(fieldName, value);
    }

    public Object detail(String fieldName) {
        return details.get(fieldName);
    }
}
