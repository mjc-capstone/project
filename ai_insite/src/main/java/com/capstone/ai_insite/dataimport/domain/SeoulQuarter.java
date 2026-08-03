package com.capstone.ai_insite.dataimport.domain;

import java.util.ArrayList;
import java.util.List;

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

    public static List<SeoulQuarter> rangeInclusive(
        String fromSourceCode,
        String toSourceCode,
        int maxQuarterCount
    ) {
        SeoulQuarter from = parse(fromSourceCode);
        SeoulQuarter to = parse(toSourceCode);
        int fromIndex = from.year() * 4 + from.quarter() - 1;
        int toIndex = to.year() * 4 + to.quarter() - 1;
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("시작 분기는 종료 분기보다 늦을 수 없습니다.");
        }
        int count = toIndex - fromIndex + 1;
        if (count > maxQuarterCount) {
            throw new IllegalArgumentException(
                "한 번에 수집할 수 있는 최대 분기 수는 " + maxQuarterCount + "개입니다."
            );
        }
        List<SeoulQuarter> quarters = new ArrayList<>(count);
        for (int index = fromIndex; index <= toIndex; index++) {
            int year = index / 4;
            int quarter = index % 4 + 1;
            quarters.add(parse(year + Integer.toString(quarter)));
        }
        return List.copyOf(quarters);
    }
}
