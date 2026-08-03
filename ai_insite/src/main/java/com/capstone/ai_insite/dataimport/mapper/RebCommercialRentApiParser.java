package com.capstone.ai_insite.dataimport.mapper;

import com.capstone.ai_insite.dataimport.domain.RebCommercialMetricType;
import com.capstone.ai_insite.dataimport.domain.RebCommercialPropertyType;
import com.capstone.ai_insite.dataimport.dto.reb.RebCommercialRentObservation;
import com.capstone.ai_insite.dataimport.dto.reb.RebCommercialRentPage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class RebCommercialRentApiParser {

    private final ObjectMapper objectMapper;

    public RebCommercialRentApiParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public RebCommercialRentPage parse(
        String responseBody,
        RebCommercialPropertyType propertyType,
        RebCommercialMetricType metricType,
        String targetPeriodIdentifier
    ) {
        try {
            JsonNode root = objectMapper.readTree(responseBody)
                .path("SttsApiTblData");
            JsonNode head = root.path(0).path("head");
            String resultCode = head.path(1).path("RESULT").path("CODE").asText();
            if (!"INFO-000".equals(resultCode)) {
                throw new IllegalArgumentException(
                    "REB API error: " + resultCode + " "
                        + head.path(1).path("RESULT").path("MESSAGE").asText()
                );
            }
            int totalCount = head.path(0).path("list_total_count").asInt();
            JsonNode rows = root.path(1).path("row");
            List<RebCommercialRentObservation> observations = new ArrayList<>();
            if (rows.isArray()) {
                rows.forEach(row -> add(
                    observations,
                    row,
                    propertyType,
                    metricType,
                    targetPeriodIdentifier
                ));
            }
            return new RebCommercialRentPage(
                totalCount,
                rows.isArray() ? rows.size() : 0,
                List.copyOf(observations)
            );
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                "REB commercial rent response parsing failed.",
                exception
            );
        }
    }

    private void add(
        List<RebCommercialRentObservation> observations,
        JsonNode row,
        RebCommercialPropertyType propertyType,
        RebCommercialMetricType metricType,
        String targetPeriodIdentifier
    ) {
        String periodIdentifier = row.path("WRTTIME_IDTFR_ID").asText();
        String itemName = row.path("ITM_NM").asText();
        String fullName = row.path("CLS_FULLNM").asText();
        if (!targetPeriodIdentifier.equals(periodIdentifier)
            || !metricType.accepts(itemName)
            || !(fullName.equals("서울") || fullName.startsWith("서울>"))
            || row.path("DTA_VAL").isNull()) {
            return;
        }
        observations.add(new RebCommercialRentObservation(
            row.path("STATBL_ID").asText(),
            periodIdentifier,
            row.path("CLS_ID").asText(),
            row.path("CLS_NM").asText(),
            fullName,
            regionLevel(fullName),
            propertyType,
            metricType,
            new BigDecimal(row.path("DTA_VAL").asText()),
            row.path("UI_NM").asText(null),
            row.path("ITM_ID").asText(null),
            itemName,
            row.toString()
        ));
    }

    private static String regionLevel(String fullName) {
        int depth = fullName.split(">", -1).length;
        if (depth == 1) {
            return "SIDO";
        }
        if (depth == 2) {
            return "REB_MARKET";
        }
        return "REB_SUBMARKET";
    }
}
