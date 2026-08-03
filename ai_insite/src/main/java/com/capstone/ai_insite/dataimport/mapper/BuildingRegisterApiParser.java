package com.capstone.ai_insite.dataimport.mapper;

import com.capstone.ai_insite.dataimport.dto.publicdata.BuildingRegisterPage;
import com.capstone.ai_insite.dataimport.dto.publicdata.BuildingRegisterRow;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class BuildingRegisterApiParser {

    private static final DateTimeFormatter BASIC_DATE =
        DateTimeFormatter.BASIC_ISO_DATE;

    private final ObjectMapper objectMapper;

    public BuildingRegisterApiParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public BuildingRegisterPage parse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody).path("response");
            JsonNode header = root.path("header");
            String resultCode = header.path("resultCode").asString();
            if (!"00".equals(resultCode)) {
                throw new IllegalArgumentException(
                    "Building register API error: " + resultCode + " "
                        + header.path("resultMsg").asString()
                );
            }
            JsonNode body = root.path("body");
            JsonNode item = body.path("items").path("item");
            List<BuildingRegisterRow> rows = new ArrayList<>();
            if (item.isArray()) {
                item.forEach(value -> rows.add(toRow(value)));
            } else if (item.isObject()) {
                rows.add(toRow(item));
            }
            return new BuildingRegisterPage(
                integer(body, "pageNo", 1),
                integer(body, "numOfRows", rows.size()),
                integer(body, "totalCount", rows.size()),
                List.copyOf(rows)
            );
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                "Building register API response parsing failed.",
                exception
            );
        }
    }

    private BuildingRegisterRow toRow(JsonNode item) {
        return new BuildingRegisterRow(
            requiredText(item, "mgmBldrgstPk"),
            requiredText(item, "sigunguCd"),
            requiredText(item, "bjdongCd"),
            text(item, "platPlc"),
            text(item, "newPlatPlc"),
            text(item, "bldNm"),
            text(item, "dongNm"),
            text(item, "regstrKindCd"),
            text(item, "regstrKindCdNm"),
            text(item, "mainAtchGbCd"),
            text(item, "mainAtchGbCdNm"),
            text(item, "mainPurpsCd"),
            text(item, "mainPurpsCdNm"),
            text(item, "etcPurps"),
            decimal(item, "platArea"),
            decimal(item, "archArea"),
            decimal(item, "totArea"),
            decimal(item, "bcRat"),
            decimal(item, "vlRat"),
            nullableInteger(item, "grndFlrCnt"),
            nullableInteger(item, "ugrndFlrCnt"),
            date(item, "useAprDay"),
            sum(item, "indrMechUtcnt", "oudrMechUtcnt",
                "indrAutoUtcnt", "oudrAutoUtcnt"),
            sum(item, "rideUseElvtCnt", "emgenUseElvtCnt"),
            date(item, "crtnDay"),
            item.toString()
        );
    }

    private static String requiredText(JsonNode parent, String name) {
        String value = text(parent, name);
        if (value == null) {
            throw new IllegalArgumentException(
                "Building register field is required: " + name
            );
        }
        return value;
    }

    private static String text(JsonNode parent, String name) {
        String value = parent.path(name).asString().trim();
        return value.isEmpty() ? null : value;
    }

    private static BigDecimal decimal(JsonNode parent, String name) {
        String value = text(parent, name);
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value.replace(",", ""));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static Integer nullableInteger(JsonNode parent, String name) {
        String value = text(parent, name);
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value).intValue();
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static int integer(JsonNode parent, String name, int fallback) {
        Integer value = nullableInteger(parent, name);
        return value == null ? fallback : value;
    }

    private static int sum(JsonNode parent, String... names) {
        int result = 0;
        for (String name : names) {
            Integer value = nullableInteger(parent, name);
            result += value == null ? 0 : Math.max(0, value);
        }
        return result;
    }

    private static LocalDate date(JsonNode parent, String name) {
        String value = text(parent, name);
        if (value == null || !value.matches("\\d{8}")) {
            return null;
        }
        try {
            return LocalDate.parse(value, BASIC_DATE);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }
}
