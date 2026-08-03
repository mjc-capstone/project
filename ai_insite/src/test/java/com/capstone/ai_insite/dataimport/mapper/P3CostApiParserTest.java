package com.capstone.ai_insite.dataimport.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.capstone.ai_insite.dataimport.domain.RebCommercialMetricType;
import com.capstone.ai_insite.dataimport.domain.RebCommercialPropertyType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class P3CostApiParserTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parsesRebStatisticsAndKeepsOnlyTargetSeoulRows() {
        String response = """
            {"SttsApiTblData":[
              {"head":[{"list_total_count":2},{"RESULT":{"CODE":"INFO-000","MESSAGE":"정상"}}]},
              {"row":[
                {"STATBL_ID":"T1","WRTTIME_IDTFR_ID":"202601","CLS_ID":510003,
                 "CLS_NM":"도심","CLS_FULLNM":"서울>도심","ITM_ID":100001,
                 "ITM_NM":"임대료","DTA_VAL":71.6221,"UI_NM":"천원/㎡"},
                {"STATBL_ID":"T1","WRTTIME_IDTFR_ID":"202601","CLS_ID":520104,
                 "CLS_NM":"충장로","CLS_FULLNM":"광주>충장로","ITM_ID":100001,
                 "ITM_NM":"임대료","DTA_VAL":18.9,"UI_NM":"천원/㎡"}
              ]}
            ]}
            """;

        var page = new RebCommercialRentApiParser(objectMapper).parse(
            response,
            RebCommercialPropertyType.SMALL_RETAIL,
            RebCommercialMetricType.RENT_AMOUNT,
            "202601"
        );

        assertEquals(2, page.sourceRowCount());
        assertEquals(1, page.observations().size());
        assertEquals("REB_MARKET", page.observations().getFirst().regionLevel());
        assertEquals(
            new BigDecimal("71.6221"),
            page.observations().getFirst().value()
        );
    }

    @Test
    void parsesMolitXmlAmountsAreasAndCancellation() {
        String response = """
            <response><header><resultCode>000</resultCode><resultMsg>OK</resultMsg></header>
            <body><items><item>
              <buildYear>2019</buildYear><buildingAr>461.28</buildingAr>
              <buildingType>일반</buildingType><buildingUse>제2종근린생활</buildingUse>
              <cdealDay></cdealDay><cdealType></cdealType><dealAmount>785,000</dealAmount>
              <dealDay>31</dealDay><dealMonth>3</dealMonth><dealYear>2026</dealYear>
              <dealingGbn>중개거래</dealingGbn><floor></floor><jibun>1**</jibun>
              <landUse>제2종일반주거</landUse><plottageAr>187.1</plottageAr>
              <sggCd>11110</sggCd><sggNm>종로구</sggNm><umdNm>통인동</umdNm>
            </item></items><numOfRows>1</numOfRows><pageNo>1</pageNo><totalCount>1</totalCount>
            </body></response>
            """;

        var page = new CommercialTransactionXmlParser(objectMapper).parse(response);
        var row = page.rows().getFirst();

        assertEquals(new BigDecimal("7850000000"), row.dealAmountKrw());
        assertEquals(new BigDecimal("461.28"), row.buildingAreaSquareMeter());
        assertEquals("통인동", row.legalDongName());
        assertFalse(row.cancelled());
        assertTrue(row.sourceRowJson().contains("buildingUse"));
    }
}
