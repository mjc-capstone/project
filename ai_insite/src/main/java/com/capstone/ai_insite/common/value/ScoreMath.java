package com.capstone.ai_insite.common.value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Objects;

public final class ScoreMath {

    public static final BigDecimal NEUTRAL = BigDecimal.valueOf(50);
    private static final BigDecimal MIN = BigDecimal.ZERO;
    private static final BigDecimal MAX = BigDecimal.valueOf(100);

    private ScoreMath() {
    }

    public static BigDecimal clamp(BigDecimal value) {
        if (value == null) {
            return NEUTRAL.setScale(4, RoundingMode.HALF_UP);
        }
        return value.max(MIN).min(MAX).setScale(4, RoundingMode.HALF_UP);
    }

    public static BigDecimal average(Collection<BigDecimal> values) {
        var available = values.stream().filter(Objects::nonNull).toList();
        if (available.isEmpty()) {
            return NEUTRAL.setScale(4, RoundingMode.HALF_UP);
        }
        BigDecimal sum = available.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return clamp(sum.divide(BigDecimal.valueOf(available.size()), 8, RoundingMode.HALF_UP));
    }

    public static BigDecimal weighted(
        BigDecimal first,
        double firstWeight,
        BigDecimal second,
        double secondWeight
    ) {
        return clamp(first.multiply(BigDecimal.valueOf(firstWeight))
            .add(second.multiply(BigDecimal.valueOf(secondWeight))));
    }
}
