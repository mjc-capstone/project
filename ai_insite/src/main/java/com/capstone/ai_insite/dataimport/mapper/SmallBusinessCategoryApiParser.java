package com.capstone.ai_insite.dataimport.mapper;

import com.capstone.ai_insite.dataimport.dto.publicdata.SmallBusinessCategoryRow;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class SmallBusinessCategoryApiParser {

    private final ObjectMapper objectMapper;

    public SmallBusinessCategoryApiParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<SmallBusinessCategoryRow> parseSmallCategories(
        String responseBody
    ) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String resultCode = root.path("header").path("resultCode").asString();
            if (!"00".equals(resultCode)) {
                throw new IllegalStateException(
                    "Small-business category API error: " + resultCode
                );
            }
            List<SmallBusinessCategoryRow> rows = new ArrayList<>();
            root.path("body").path("items").forEach(item -> rows.add(
                new SmallBusinessCategoryRow(
                    text(item, "indsLclsCd"),
                    text(item, "indsLclsNm"),
                    text(item, "indsMclsCd"),
                    text(item, "indsMclsNm"),
                    text(item, "indsSclsCd"),
                    text(item, "indsSclsNm"),
                    parseDate(text(item, "stdrDt"))
                )
            ));
            return List.copyOf(rows);
        } catch (Exception exception) {
            throw new IllegalStateException(
                "Failed to parse small-business category response.",
                exception
            );
        }
    }

    public List<CodeName> parseCodes(
        String responseBody,
        String codeField,
        String nameField
    ) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            if (!"00".equals(
                root.path("header").path("resultCode").asString()
            )) {
                throw new IllegalStateException("Category hierarchy API error.");
            }
            List<CodeName> rows = new ArrayList<>();
            root.path("body").path("items").forEach(item -> rows.add(
                new CodeName(text(item, codeField), text(item, nameField))
            ));
            return List.copyOf(rows);
        } catch (Exception exception) {
            throw new IllegalStateException(
                "Failed to parse category hierarchy response.",
                exception
            );
        }
    }

    private static String text(JsonNode node, String field) {
        String value = node.path(field).asString();
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static LocalDate parseDate(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }

    public record CodeName(String code, String name) {
    }
}
