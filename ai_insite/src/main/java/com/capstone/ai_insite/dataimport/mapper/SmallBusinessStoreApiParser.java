package com.capstone.ai_insite.dataimport.mapper;

import com.capstone.ai_insite.dataimport.dto.publicdata.SmallBusinessStoreApiItem;
import com.capstone.ai_insite.dataimport.dto.publicdata.SmallBusinessStoreApiPage;
import com.capstone.ai_insite.dataimport.dto.publicdata.SmallBusinessStoreApiRow;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class SmallBusinessStoreApiParser {

    private final ObjectMapper objectMapper;

    public SmallBusinessStoreApiParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SmallBusinessStoreApiPage parse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode header = root.path("header");
            String resultCode = header.path("resultCode").asString();
            if (!"00".equals(resultCode)) {
                throw new IllegalStateException(
                    "상가정보 API가 오류를 반환했습니다: "
                        + resultCode + " " + header.path("resultMsg").asString()
                );
            }
            JsonNode body = root.path("body");
            List<SmallBusinessStoreApiRow> rows = new ArrayList<>();
            body.path("items").forEach(item -> rows.add(
                new SmallBusinessStoreApiRow(
                    objectMapper.treeToValue(item, SmallBusinessStoreApiItem.class),
                    item.toString()
                )
            ));
            return new SmallBusinessStoreApiPage(
                header.path("stdrYm").asString(),
                body.path("pageNo").asInt(),
                body.path("numOfRows").asInt(rows.size()),
                body.path("totalCount").asInt(rows.size()),
                List.copyOf(rows)
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                "상가정보 API 응답 해석에 실패했습니다.",
                exception
            );
        }
    }
}
