package com.capstone.ai_insite.dataimport.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class BuildingRegisterApiParserTest {

    private final BuildingRegisterApiParser parser =
        new BuildingRegisterApiParser(new ObjectMapper());

    @Test
    void parsesRealBuildingHubTitleShape() {
        String response = """
            {
              "response": {
                "header": {"resultCode":"00","resultMsg":"NORMAL SERVICE"},
                "body": {
                  "items": {"item": [{
                    "platPlc":"서울특별시 종로구 청운동 1번지",
                    "sigunguCd":"11110",
                    "bjdongCd":"10100",
                    "mgmBldrgstPk":100212383,
                    "regstrKindCd":"3",
                    "regstrKindCdNm":"표제부",
                    "newPlatPlc":"서울특별시 종로구 자하문로36길 16-14",
                    "bldNm":"테스트빌딩",
                    "dongNm":"1동",
                    "mainAtchGbCd":"0",
                    "mainAtchGbCdNm":"주건축물",
                    "platArea":500.25,
                    "archArea":200.10,
                    "bcRat":40.00,
                    "totArea":1200.50,
                    "vlRat":180.20,
                    "mainPurpsCd":"03000",
                    "mainPurpsCdNm":"제1종근린생활시설",
                    "etcPurps":"소매점",
                    "grndFlrCnt":5,
                    "ugrndFlrCnt":1,
                    "rideUseElvtCnt":2,
                    "emgenUseElvtCnt":1,
                    "indrMechUtcnt":3,
                    "oudrMechUtcnt":2,
                    "indrAutoUtcnt":10,
                    "oudrAutoUtcnt":5,
                    "useAprDay":"19990102",
                    "crtnDay":"20260710"
                  }]},
                  "numOfRows":"1",
                  "pageNo":"1",
                  "totalCount":"1"
                }
              }
            }
            """;

        var page = parser.parse(response);
        var row = page.rows().getFirst();

        assertEquals(1, page.totalCount());
        assertEquals("100212383", row.buildingRegisterId());
        assertEquals("제1종근린생활시설", row.mainUseName());
        assertEquals(
            0,
            new BigDecimal("1200.50").compareTo(row.grossFloorAreaSquareMeter())
        );
        assertEquals(20, row.parkingCount());
        assertEquals(3, row.elevatorCount());
        assertEquals("1999-01-02", row.approvalDate().toString());
        assertTrue(row.sourceRowJson().contains("mainPurpsCd"));
    }

    @Test
    void acceptsSingleItemObjectAndBlankDates() {
        String response = """
            {"response":{"header":{"resultCode":"00"},"body":{
              "items":{"item":{"mgmBldrgstPk":"PK-1","sigunguCd":"11110",
              "bjdongCd":"10100","useAprDay":" "}},
              "pageNo":1,"numOfRows":1,"totalCount":1}}}
            """;

        var page = parser.parse(response);

        assertEquals(1, page.rows().size());
        assertEquals(null, page.rows().getFirst().approvalDate());
    }
}
