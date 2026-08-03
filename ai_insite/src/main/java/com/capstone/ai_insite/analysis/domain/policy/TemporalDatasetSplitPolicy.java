package com.capstone.ai_insite.analysis.domain.policy;

import com.capstone.ai_insite.analysis.domain.DatasetSplit;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class TemporalDatasetSplitPolicy {

    public DatasetSplit assign(
        LocalDate featureAsOfDate,
        LocalDate labelPeriodStart,
        LocalDate labelPeriodEnd,
        LocalDate trainThrough,
        LocalDate validationThrough,
        LocalDate testThrough
    ) {
        validateBoundaries(trainThrough, validationThrough, testThrough);
        if (!featureAsOfDate.isBefore(labelPeriodStart)) {
            throw new IllegalArgumentException("피처 기준일은 라벨 대상 분기보다 이전이어야 합니다.");
        }
        if (!labelPeriodEnd.isAfter(trainThrough)) {
            return DatasetSplit.TRAIN;
        }
        if (!labelPeriodEnd.isAfter(validationThrough)) {
            return DatasetSplit.VALIDATION;
        }
        if (!labelPeriodEnd.isAfter(testThrough)) {
            return DatasetSplit.TEST;
        }
        throw new IllegalArgumentException("라벨 대상 분기가 테스트 범위를 벗어났습니다.");
    }

    public void validateBoundaries(
        LocalDate trainThrough,
        LocalDate validationThrough,
        LocalDate testThrough
    ) {
        if (!trainThrough.isBefore(validationThrough)
            || !validationThrough.isBefore(testThrough)) {
            throw new IllegalArgumentException(
                "Train, Validation, Test 종료 분기는 시간순이어야 합니다."
            );
        }
    }
}
