package com.capstone.ai_insite.dataimport.mapper;

import com.capstone.ai_insite.dataimport.dto.publicdata.StandardLegalDongPage;
import com.capstone.ai_insite.dataimport.dto.publicdata.StandardLegalDongRow;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class StandardLegalDongApiParser {

    private static final DateTimeFormatter BASIC_DATE =
        DateTimeFormatter.BASIC_ISO_DATE;

    private final ObjectMapper objectMapper;

    public StandardLegalDongApiParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public StandardLegalDongPage parse(String responseBody) {
        try {
            JsonNode data = objectMapper.readTree(responseBody)
                .path("StanReginCd");
            JsonNode head = data.get(0).path("head");
            String resultCode = head.get(2).path("RESULT")
                .path("resultCode").asString();
            if (!"INFO-0".equals(resultCode)) {
                throw new IllegalStateException(
                    "MOIS legal-dong API error: " + resultCode
                );
            }
            int totalCount = head.get(0).path("totalCount").asInt();
            int pageNumber = head.get(1).path("pageNo").asInt();
            int numberOfRows = head.get(1).path("numOfRows").asInt();
            List<StandardLegalDongRow> rows = new ArrayList<>();
            data.get(1).path("row").forEach(row -> {
                String code = text(row, "region_cd");
                String address = text(row, "locatadd_nm");
                String umdCode = text(row, "umd_cd");
                if (code == null
                    || address == null
                    || "000".equals(umdCode)
                    || !code.startsWith("11")) {
                    return;
                }
                String[] names = address.split("\\s+");
                if (names.length < 3) {
                    return;
                }
                rows.add(new StandardLegalDongRow(
                    code,
                    text(row, "sido_cd"),
                    names[0],
                    code.substring(0, 5),
                    names[1],
                    names[names.length - 1],
                    parseDate(text(row, "adpt_de"))
                ));
            });
            return new StandardLegalDongPage(
                totalCount,
                pageNumber,
                numberOfRows,
                data.get(1).path("row").size(),
                List.copyOf(rows)
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                "Failed to parse MOIS legal-dong response.",
                exception
            );
        }
    }

    private static String text(JsonNode node, String field) {
        String value = node.path(field).asString();
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static LocalDate parseDate(String value) {
        return value == null || !value.matches("\\d{8}")
            ? null
            : LocalDate.parse(value, BASIC_DATE);
    }
}
