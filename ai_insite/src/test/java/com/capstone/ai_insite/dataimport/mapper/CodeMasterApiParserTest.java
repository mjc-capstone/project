package com.capstone.ai_insite.dataimport.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class CodeMasterApiParserTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parsesActualMoisLegalDongShapeAndExcludesDistrictRows() {
        var parser = new StandardLegalDongApiParser(objectMapper);
        var page = parser.parse("""
            {"StanReginCd":[{"head":[{"totalCount":2},
            {"numOfRows":"1000","pageNo":"1","type":"JSON"},
            {"RESULT":{"resultCode":"INFO-0","resultMsg":"NOMAL SERVICE"}}]},
            {"row":[
            {"region_cd":"1171000000","sido_cd":"11","sgg_cd":"710",
             "umd_cd":"000","locatadd_nm":"서울특별시 송파구","adpt_de":""},
            {"region_cd":"1171010100","sido_cd":"11","sgg_cd":"710",
             "umd_cd":"101","locatadd_nm":"서울특별시 송파구 잠실동",
             "adpt_de":"19880401"}]}]}
            """);

        assertEquals(2, page.sourceRowCount());
        assertEquals(1, page.rows().size());
        assertEquals("1171010100", page.rows().getFirst().legalDongCode());
        assertEquals("잠실동", page.rows().getFirst().legalDongName());
    }

    @Test
    void parsesActualSmallBusinessCategoryShape() {
        var parser = new SmallBusinessCategoryApiParser(objectMapper);
        var rows = parser.parseSmallCategories("""
            {"header":{"resultCode":"00","resultMsg":"NORMAL SERVICE"},
            "body":{"items":[{
              "indsLclsCd":"I2","indsLclsNm":"음식",
              "indsMclsCd":"I201","indsMclsNm":"한식",
              "indsSclsCd":"I20101","indsSclsNm":"백반/한정식",
              "stdrDt":"2023-02-28"}]}}
            """);

        assertEquals(1, rows.size());
        assertEquals("I20101", rows.getFirst().smallCategoryCode());
        assertEquals("백반/한정식", rows.getFirst().smallCategoryName());
    }
}
