package com.capstone.ai_insite.dataimport.mapper;

import com.capstone.ai_insite.dataimport.domain.SmallBusinessStoreImportCommand;
import com.capstone.ai_insite.dataimport.dto.publicdata.SmallBusinessStoreApiRow;
import java.time.LocalDate;
import java.time.YearMonth;
import org.springframework.stereotype.Component;

@Component
public class SmallBusinessStoreRowMapper {

    public SmallBusinessStoreImportCommand toCommand(
        SmallBusinessStoreApiRow source,
        String standardMonth
    ) {
        var row = source.value();
        LocalDate snapshotDate = parseMonth(standardMonth).atEndOfMonth();
        return new SmallBusinessStoreImportCommand(
            trim(row.getBizesId()),
            storeName(row.getBizesNm(), row.getBrchNm()),
            trim(row.getIndsLclsCd()),
            trim(row.getIndsLclsNm()),
            trim(row.getIndsMclsCd()),
            trim(row.getIndsMclsNm()),
            trim(row.getIndsSclsCd()),
            trim(row.getIndsSclsNm()),
            trim(row.getKsicCd()),
            trim(row.getKsicNm()),
            trim(row.getAdongCd()),
            trim(row.getLdongCd()),
            trim(row.getLnoAdr()),
            trim(row.getRdnmAdr()),
            row.getLon(),
            row.getLat(),
            snapshotDate,
            null,
            source.sourceJson()
        );
    }

    public YearMonth parseMonth(String standardMonth) {
        if (standardMonth == null || !standardMonth.matches("\\d{6}")) {
            throw new IllegalArgumentException(
                "상가정보 기준월이 올바르지 않습니다: " + standardMonth
            );
        }
        return YearMonth.of(
            Integer.parseInt(standardMonth.substring(0, 4)),
            Integer.parseInt(standardMonth.substring(4, 6))
        );
    }

    private static String storeName(String name, String branchName) {
        String normalizedName = trim(name);
        String normalizedBranch = trim(branchName);
        if (normalizedBranch == null) {
            return normalizedName;
        }
        return normalizedName == null
            ? normalizedBranch
            : normalizedName + " " + normalizedBranch;
    }

    private static String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
