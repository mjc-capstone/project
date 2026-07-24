package com.capstone.ai_insite.dataimport.domain;

import java.math.BigDecimal;

public sealed interface SeoulRegionalImportCommand
    extends SeoulRegionPeriodReference {

    String periodCode();

    String sourceRowJson();

    record FloatingPopulation(
        String regionCode,
        String regionName,
        String periodCode,
        String sourcePeriodCode,
        Long total,
        String byGenderJson,
        String byAgeJson,
        String byDayJson,
        String byTimeJson,
        String sourceRowJson
    ) implements SeoulRegionalImportCommand {
    }

    record ResidentPopulation(
        String regionCode,
        String regionName,
        String periodCode,
        String sourcePeriodCode,
        Long total,
        String byGenderJson,
        String byAgeJson,
        Long householdCount,
        String householdTypeJson,
        String sourceRowJson
    ) implements SeoulRegionalImportCommand {
    }

    record WorkingPopulation(
        String regionCode,
        String regionName,
        String periodCode,
        String sourcePeriodCode,
        Long total,
        Long male,
        Long female,
        String byAgeJson,
        String maleByAgeJson,
        String femaleByAgeJson,
        String sourceRowJson
    ) implements SeoulRegionalImportCommand {
    }

    record Facilities(
        String regionCode,
        String regionName,
        String periodCode,
        String sourcePeriodCode,
        Integer totalCount,
        Integer publicOfficeCount,
        Integer bankCount,
        Integer generalHospitalCount,
        Integer hospitalCount,
        Integer pharmacyCount,
        Integer kindergartenCount,
        Integer elementarySchoolCount,
        Integer middleSchoolCount,
        Integer highSchoolCount,
        Integer universityCount,
        Integer departmentStoreCount,
        Integer supermarketCount,
        Integer theaterCount,
        Integer lodgingCount,
        Integer railwayStationCount,
        Integer subwayStationCount,
        Integer busStopCount,
        String detailJson,
        String sourceRowJson
    ) implements SeoulRegionalImportCommand {
    }

    record Apartments(
        String regionCode,
        String regionName,
        String periodCode,
        String sourcePeriodCode,
        Integer complexCount,
        String householdsByAreaJson,
        String householdsByPriceJson,
        BigDecimal averageArea,
        Long averageMarketPrice,
        String sourceRowJson
    ) implements SeoulRegionalImportCommand {
    }
}
