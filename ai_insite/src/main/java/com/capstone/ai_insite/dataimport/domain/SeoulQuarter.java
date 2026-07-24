package com.capstone.ai_insite.dataimport.domain;

public record SeoulQuarter(
    String sourceCode,
    String periodCode,
    int year,
    int quarter
) {
    public static SeoulQuarter parse(String sourceCode) {
        if (sourceCode == null || !sourceCode.matches("\\d{5}")) {
            throw new IllegalArgumentException("서울시 기준 년분기 코드가 올바르지 않습니다.");
        }
        int year = Integer.parseInt(sourceCode.substring(0, 4));
        int quarter = Integer.parseInt(sourceCode.substring(4));
        if (quarter < 1 || quarter > 4) {
            throw new IllegalArgumentException("서울시 분기 값은 1~4 범위여야 합니다.");
        }
        return new SeoulQuarter(sourceCode, year + "Q" + quarter, year, quarter);
    }
}
