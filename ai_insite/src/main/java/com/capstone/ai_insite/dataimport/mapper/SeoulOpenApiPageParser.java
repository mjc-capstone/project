package com.capstone.ai_insite.dataimport.mapper;

import com.capstone.ai_insite.dataimport.dto.seoul.SeoulApiPage;
import com.capstone.ai_insite.dataimport.dto.seoul.SeoulApiRow;
import com.capstone.ai_insite.dataimport.dto.seoul.SeoulRegionalApiRow;
import com.capstone.ai_insite.dataimport.dto.seoul.SeoulSalesApiRow;
import com.capstone.ai_insite.dataimport.dto.seoul.SeoulStoresApiRow;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class SeoulOpenApiPageParser {

    public static final String SALES_SERVICE = "VwsmAdstrdSelngW";
    public static final String STORES_SERVICE = "VwsmAdstrdStorW";
    public static final String FLOATING_POPULATION_SERVICE = "VwsmAdstrdFlpopW";
    public static final String RESIDENT_POPULATION_SERVICE = "VwsmAdstrdRepopW";
    public static final String WORKING_POPULATION_SERVICE = "VwsmAdstrdWrcPopltnW";
    public static final String FACILITIES_SERVICE = "VwsmAdstrdFcltyW";
    public static final String APARTMENTS_SERVICE = "VwsmAdstrdAptW";

    private final ObjectMapper objectMapper;

    public SeoulOpenApiPageParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SeoulApiPage<SeoulSalesApiRow> parseSales(String responseBody) {
        return parse(responseBody, SALES_SERVICE, SeoulSalesApiRow.class);
    }

    public SeoulApiPage<SeoulStoresApiRow> parseStores(String responseBody) {
        return parse(responseBody, STORES_SERVICE, SeoulStoresApiRow.class);
    }

    public SeoulApiPage<SeoulRegionalApiRow> parseRegional(
        String responseBody,
        String serviceName
    ) {
        return parse(responseBody, serviceName, SeoulRegionalApiRow.class);
    }

    private <T> SeoulApiPage<T> parse(
        String responseBody,
        String serviceName,
        Class<T> rowType
    ) {
        try {
            JsonNode service = objectMapper.readTree(responseBody).path(serviceName);
            if (service.isMissingNode()) {
                throw new IllegalStateException(
                    "서울 API 응답에 서비스 노드가 없습니다: " + serviceName
                );
            }
            String resultCode = service.path("RESULT").path("CODE").asString();
            if (!"INFO-000".equals(resultCode)) {
                throw new IllegalStateException(
                    "서울 API가 오류를 반환했습니다: " + resultCode
                );
            }
            List<SeoulApiRow<T>> rows = new ArrayList<>();
            service.path("row").forEach(row -> rows.add(new SeoulApiRow<>(
                objectMapper.treeToValue(row, rowType),
                row.toString()
            )));
            return new SeoulApiPage<>(
                service.path("list_total_count").asInt(rows.size()),
                List.copyOf(rows)
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                "서울 API " + serviceName + " 응답 해석에 실패했습니다.",
                exception
            );
        }
    }
}
