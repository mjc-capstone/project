package com.capstone.ai_insite.dataimport.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class SmallBusinessStoreApiMappingTest {

    private final SmallBusinessStoreApiParser parser =
        new SmallBusinessStoreApiParser(new ObjectMapper());
    private final SmallBusinessStoreRowMapper mapper =
        new SmallBusinessStoreRowMapper();

    @Test
    void mapsActualPublicDataResponseShapeAndKeepsSourceJson() {
        var page = parser.parse("""
            {
              "header": {
                "description": "success",
                "resultCode": "00",
                "resultMsg": "NORMAL SERVICE",
                "stdrYm": "202603"
              },
              "body": {
                "items": [{
                  "bizesId": "MA010120220805430875",
                  "bizesNm": "고향집",
                  "brchNm": "",
                  "indsLclsCd": "I2",
                  "indsLclsNm": "음식",
                  "indsMclsCd": "I201",
                  "indsMclsNm": "한식",
                  "indsSclsCd": "I20101",
                  "indsSclsNm": "백반/한정식",
                  "ksicCd": "I56111",
                  "ksicNm": "한식 일반 음식점업",
                  "adongCd": "11110615",
                  "ldongCd": "1111010100",
                  "lnoAdr": "서울특별시 종로구 청운동 1",
                  "rdnmAdr": "서울특별시 종로구 자하문로 1",
                  "lon": 126.96912345,
                  "lat": 37.58712345
                }],
                "numOfRows": 1,
                "pageNo": 1,
                "totalCount": 20932
              }
            }
            """);

        var command = mapper.toCommand(page.rows().getFirst(), page.standardMonth());

        assertEquals("202603", page.standardMonth());
        assertEquals(20_932, page.totalCount());
        assertEquals("MA010120220805430875", command.externalStoreId());
        assertEquals("I20101", command.sourceSmallCategoryCode());
        assertEquals("11110615", command.administrativeDongCode());
        assertEquals(new BigDecimal("126.96912345"), command.longitude());
        assertEquals(LocalDate.of(2026, 3, 31), command.snapshotDate());
        assertTrue(command.sourceRowJson().contains("\"bizesNm\":\"고향집\""));
    }

    @Test
    void rejectsNonSuccessResponse() {
        assertThrows(IllegalStateException.class, () -> parser.parse("""
            {
              "header": {
                "resultCode": "03",
                "resultMsg": "NO DATA",
                "stdrYm": "202603"
              },
              "body": {"items": []}
            }
            """));
    }
}
