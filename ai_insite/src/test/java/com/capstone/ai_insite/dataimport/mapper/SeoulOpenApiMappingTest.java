package com.capstone.ai_insite.dataimport.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.capstone.ai_insite.dataimport.domain.SalesImportCommand;
import com.capstone.ai_insite.dataimport.domain.StoreImportCommand;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class SeoulOpenApiMappingTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SeoulOpenApiPageParser parser = new SeoulOpenApiPageParser(objectMapper);

    @Test
    void mapsSalesRowAndPreservesBreakdownJson() {
        String response = """
            {
              "VwsmAdstrdSelngW": {
                "list_total_count": 1,
                "RESULT": {"CODE": "INFO-000", "MESSAGE": "정상 처리되었습니다"},
                "row": [{
                  "STDR_YYQU_CD": "20261",
                  "ADSTRD_CD": "11710720",
                  "ADSTRD_CD_NM": "잠실7동",
                  "SVC_INDUTY_CD": "CS100001",
                  "SVC_INDUTY_CD_NM": "한식음식점",
                  "THSMON_SELNG_AMT": 23947025,
                  "THSMON_SELNG_CO": 1574.0,
                  "MDWK_SELNG_AMT": 21587186,
                  "WKEND_SELNG_AMT": 2359839.0,
                  "MON_SELNG_AMT": 4056408.0,
                  "MON_SELNG_CO": 246.0,
                  "TMZON_11_14_SELNG_AMT": 17664188,
                  "TMZON_11_14_SELNG_CO": 1252.0,
                  "ML_SELNG_AMT": 10140200,
                  "AGRDE_40_SELNG_CO": 357.0
                }]
              }
            }
            """;

        var page = parser.parseSales(response);
        SalesImportCommand command = new SeoulSalesRowMapper(objectMapper)
            .toCommand(page.rows().getFirst());

        assertEquals(1, page.totalCount());
        assertEquals("2026Q1", command.periodCode());
        assertEquals("20261", command.sourcePeriodCode());
        assertEquals(23_947_025L, command.salesAmount());
        assertEquals(1_574L, command.salesCount());
        assertTrue(command.salesByDayJson().contains("4056408.0"));
        assertTrue(command.salesByTimeJson().contains("17664188"));
        assertTrue(command.salesByDemographicJson().contains("357.0"));
        assertTrue(command.sourceRowJson().contains("THSMON_SELNG_AMT"));
    }

    @Test
    void mapsStoresRowUsingSimilarIndustryCountAsTotalStoreCount() {
        String response = """
            {
              "VwsmAdstrdStorW": {
                "list_total_count": 1,
                "RESULT": {"CODE": "INFO-000", "MESSAGE": "정상 처리되었습니다"},
                "row": [{
                  "STDR_YYQU_CD": "20261",
                  "ADSTRD_CD": "11110515",
                  "ADSTRD_CD_NM": "청운효자동",
                  "SVC_INDUTY_CD": "CS100003",
                  "SVC_INDUTY_CD_NM": "일식음식점",
                  "SIMILR_INDUTY_STOR_CO": 19.0,
                  "STOR_CO": 18.0,
                  "FRC_STOR_CO": 1.0,
                  "OPBIZ_RT": 11.0,
                  "OPBIZ_STOR_CO": 2.0,
                  "CLSBIZ_RT": 11.0,
                  "CLSBIZ_STOR_CO": 2.0
                }]
              }
            }
            """;

        var page = parser.parseStores(response);
        StoreImportCommand command = new SeoulStoresRowMapper()
            .toCommand(page.rows().getFirst());

        assertEquals("2026Q1", command.periodCode());
        assertEquals(19, command.storeCount());
        assertEquals(18, command.normalStoreCount());
        assertEquals(1, command.franchiseStoreCount());
        assertEquals(new BigDecimal("11.0"), command.closeRate());
    }
}
