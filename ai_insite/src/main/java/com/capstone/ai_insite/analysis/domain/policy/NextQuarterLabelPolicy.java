package com.capstone.ai_insite.analysis.domain.policy;

import com.capstone.ai_insite.analysis.domain.ModelLabelDecision;
import com.capstone.ai_insite.analysis.domain.ModelLabelObservation;
import com.capstone.ai_insite.analysis.domain.ModelLabelValues;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class NextQuarterLabelPolicy {

    public ModelLabelDecision calculate(
        ModelLabelObservation current,
        ModelLabelObservation nextQuarter,
        ModelLabelObservation fourQuartersLater
    ) {
        requireConsecutive(current, nextQuarter);
        if (!hasRequiredValues(current, nextQuarter)) {
            return ModelLabelDecision.incomplete();
        }

        BigDecimal retentionRate = retentionRate(current, fourQuartersLater);
        Boolean storeBaseMaintained = retentionRate == null
            ? null
            : retentionRate.compareTo(BigDecimal.valueOf(100)) >= 0;
        return ModelLabelDecision.ready(new ModelLabelValues(
            nextQuarter.periodCode(),
            growthRate(current.salesAmount(), nextQuarter.salesAmount()),
            nextQuarter.storeCount() < current.storeCount(),
            nextQuarter.closeRate(),
            fourQuartersLater == null ? null : fourQuartersLater.periodCode(),
            retentionRate,
            storeBaseMaintained
        ));
    }

    private static void requireConsecutive(
        ModelLabelObservation current,
        ModelLabelObservation nextQuarter
    ) {
        if (current == null || nextQuarter == null) {
            throw new IllegalArgumentException("현재 분기와 다음 분기 관측값은 필수입니다.");
        }
        if (!current.startDate().plusMonths(3).equals(nextQuarter.startDate())) {
            throw new IllegalArgumentException("라벨은 정확히 다음 분기 데이터로만 생성할 수 있습니다.");
        }
    }

    private static boolean hasRequiredValues(
        ModelLabelObservation current,
        ModelLabelObservation nextQuarter
    ) {
        return current.salesAmount() != null
            && current.salesAmount() > 0
            && nextQuarter.salesAmount() != null
            && current.storeCount() != null
            && nextQuarter.storeCount() != null
            && nextQuarter.closeRate() != null;
    }

    private static BigDecimal growthRate(long current, long future) {
        return BigDecimal.valueOf(future - current)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(current), 4, RoundingMode.HALF_UP);
    }

    private static BigDecimal retentionRate(
        ModelLabelObservation current,
        ModelLabelObservation future
    ) {
        if (future == null
            || current.storeCount() == null
            || current.storeCount() <= 0
            || future.storeCount() == null) {
            return null;
        }
        return BigDecimal.valueOf(future.storeCount())
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(current.storeCount()), 4, RoundingMode.HALF_UP);
    }
}
