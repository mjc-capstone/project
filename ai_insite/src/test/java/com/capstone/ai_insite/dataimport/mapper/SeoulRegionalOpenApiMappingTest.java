package com.capstone.ai_insite.dataimport.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.capstone.ai_insite.dataimport.domain.SeoulRegionalImportCommand;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class SeoulRegionalOpenApiMappingTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SeoulOpenApiPageParser parser = new SeoulOpenApiPageParser(objectMapper);
    private final SeoulRegionalRowMapper mapper = new SeoulRegionalRowMapper(objectMapper);

    @Test
    void mapsActualFloatingPopulationResponseShape() {
        var page = parser.parseRegional("""
            {"VwsmAdstrdFlpopW":{"list_total_count":8925,
            "RESULT":{"CODE":"INFO-000","MESSAGE":"정상 처리되었습니다"},
            "row":[{"STDR_YYQU_CD":"20253","ADSTRD_CD":"11380552",
            "ADSTRD_CD_NM":"갈현2동","TOT_FLPOP_CO":5745221.0,
            "ML_FLPOP_CO":2579270.0,"FML_FLPOP_CO":3165953.0,
            "AGRDE_20_FLPOP_CO":779266.0,"TMZON_06_11_FLPOP_CO":1191645.0,
            "SAT_FLPOP_CO":831946.0,"SUN_FLPOP_CO":848424.0}]}}
            """, SeoulOpenApiPageParser.FLOATING_POPULATION_SERVICE);

        SeoulRegionalImportCommand.FloatingPopulation command =
            mapper.toFloatingPopulation(page.rows().getFirst());

        assertEquals(8_925, page.totalCount());
        assertEquals("2025Q3", command.periodCode());
        assertEquals(5_745_221L, command.total());
        assertTrue(command.byGenderJson().contains("2579270.0"));
        assertTrue(command.byAgeJson().contains("779266.0"));
        assertTrue(command.byTimeJson().contains("1191645.0"));
        assertTrue(command.byDayJson().contains("848424.0"));
    }

    @Test
    void mapsActualResidentAndWorkingPopulationResponseShapes() {
        var residentPage = parser.parseRegional("""
            {"VwsmAdstrdRepopW":{"list_total_count":8925,
            "RESULT":{"CODE":"INFO-000","MESSAGE":"정상 처리되었습니다"},
            "row":[{"STDR_YYQU_CD":"20254","ADSTRD_CD":"11305575",
            "ADSTRD_CD_NM":"삼각산동","TOT_REPOP_CO":30604.0,
            "ML_REPOP_CO":14688.0,"FML_REPOP_CO":15916.0,
            "AGRDE_40_REPOP_CO":5643.0,"TOT_HSHLD_CO":11515.0,
            "APT_HSHLD_CO":0.0,"NON_APT_HSHLD_CO":11515.0}]}}
            """, SeoulOpenApiPageParser.RESIDENT_POPULATION_SERVICE);
        var workingPage = parser.parseRegional("""
            {"VwsmAdstrdWrcPopltnW":{"list_total_count":414,
            "RESULT":{"CODE":"INFO-000","MESSAGE":"정상 처리되었습니다"},
            "row":[{"STDR_YYQU_CD":"20261","ADSTRD_CD":"11110560",
            "ADSTRD_CD_NM":"평창동","TOT_WRC_POPLTN_CO":14903.0,
            "ML_WRC_POPLTN_CO":8370.0,"FML_WRC_POPLTN_CO":6533.0,
            "AGRDE_30_WRC_POPLTN_CO":4002.0,"MAG_30_WRC_POPLTN_CO":2053.0,
            "FAG_30_WRC_POPLTN_CO":1949.0}]}}
            """, SeoulOpenApiPageParser.WORKING_POPULATION_SERVICE);

        SeoulRegionalImportCommand.ResidentPopulation resident =
            mapper.toResidentPopulation(residentPage.rows().getFirst());
        SeoulRegionalImportCommand.WorkingPopulation working =
            mapper.toWorkingPopulation(workingPage.rows().getFirst());

        assertEquals(30_604L, resident.total());
        assertEquals(11_515L, resident.householdCount());
        assertTrue(resident.householdTypeJson().contains("NON_APT_HSHLD_CO"));
        assertEquals("2026Q1", working.periodCode());
        assertEquals(14_903L, working.total());
        assertEquals(8_370L, working.male());
        assertTrue(working.femaleByAgeJson().contains("1949.0"));
    }

    @Test
    void mapsActualFacilitiesAndApartmentsResponseShapes() {
        var facilitiesPage = parser.parseRegional("""
            {"VwsmAdstrdFcltyW":{"list_total_count":8925,
            "RESULT":{"CODE":"INFO-000","MESSAGE":"정상 처리되었습니다"},
            "row":[{"STDR_YYQU_CD":"20253","ADSTRD_CD":"11530560",
            "ADSTRD_CD_NM":"구로5동","VIATR_FCLTY_CO":167.0,
            "PBLOFC_CO":5.0,"BANK_CO":4.0,"GEHSPT_CO":0.0,
            "GNRL_HSPTL_CO":2.0,"PARMACY_CO":18.0,"KNDRGR_CO":0.0,
            "ELESCH_CO":3.0,"MSKUL_CO":1.0,"HGSCHL_CO":1.0,
            "UNIV_CO":2.0,"DRTS_CO":1.0,"SUPMK_CO":0.0,
            "THEAT_CO":3.0,"STAYNG_FCLTY_CO":0.0,"RLROAD_STATN_CO":0.0,
            "SUBWAY_STATN_CO":1.0,"BUS_STTN_CO":49.0}]}}
            """, SeoulOpenApiPageParser.FACILITIES_SERVICE);
        var apartmentsPage = parser.parseRegional("""
            {"VwsmAdstrdAptW":{"list_total_count":8907,
            "RESULT":{"CODE":"INFO-000","MESSAGE":"정상 처리되었습니다"},
            "row":[{"STDR_YYQU_CD":"20254","ADSTRD_CD":"11650660",
            "ADSTRD_CD_NM":"내곡동","APT_HSMP_CO":90.0,
            "AE_66_SQMT_BELO_HSHLD_CO":453.0,"AE_99_SQMT_HSHLD_CO":201.0,
            "PC_5_HDMIL_HSHLD_CO":151.0,"PC_6_HDMIL_ABOVE_HSHLD_CO":793.0,
            "AVRG_AE":82.0,"AVRG_MKTC":892412998}]}}
            """, SeoulOpenApiPageParser.APARTMENTS_SERVICE);

        SeoulRegionalImportCommand.Facilities facilities =
            mapper.toFacilities(facilitiesPage.rows().getFirst());
        SeoulRegionalImportCommand.Apartments apartments =
            mapper.toApartments(apartmentsPage.rows().getFirst());

        assertEquals(167, facilities.totalCount());
        assertEquals(1, facilities.subwayStationCount());
        assertEquals(49, facilities.busStopCount());
        assertTrue(facilities.detailJson().contains("PARMACY_CO"));
        assertEquals(90, apartments.complexCount());
        assertEquals(new BigDecimal("82.0"), apartments.averageArea());
        assertEquals(892_412_998L, apartments.averageMarketPrice());
        assertTrue(apartments.householdsByPriceJson().contains("793.0"));
    }
}
