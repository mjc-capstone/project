package com.capstone.ai_insite.metric.domain.policy;

import com.capstone.ai_insite.metric.domain.CommercialPriceStatistics;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public class CommercialPriceStatisticsPolicy {

    public CommercialPriceStatistics calculate(
        int transactionCount,
        List<BigDecimal> pricesPerArea
    ) {
        List<BigDecimal> sorted = pricesPerArea.stream()
            .filter(Objects::nonNull)
            .filter(value -> value.signum() > 0)
            .sorted()
            .toList();
        if (sorted.isEmpty()) {
            return new CommercialPriceStatistics(
                transactionCount,
                0,
                null,
                null,
                null,
                null
            );
        }
        BigDecimal average = sorted.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(sorted.size()), 2, RoundingMode.HALF_UP);
        return new CommercialPriceStatistics(
            transactionCount,
            sorted.size(),
            percentile(sorted, new BigDecimal("0.50")),
            average,
            percentile(sorted, new BigDecimal("0.25")),
            percentile(sorted, new BigDecimal("0.75"))
        );
    }

    public BigDecimal growthRate(
        BigDecimal current,
        BigDecimal previous
    ) {
        if (current == null || previous == null || previous.signum() == 0) {
            return null;
        }
        return current.subtract(previous)
            .multiply(BigDecimal.valueOf(100))
            .divide(previous.abs(), 4, RoundingMode.HALF_UP);
    }

    private static BigDecimal percentile(
        List<BigDecimal> sorted,
        BigDecimal percentile
    ) {
        if (sorted.size() == 1) {
            return sorted.getFirst().setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal position = BigDecimal.valueOf(sorted.size() - 1L)
            .multiply(percentile);
        int lowerIndex = position.setScale(0, RoundingMode.FLOOR).intValue();
        int upperIndex = position.setScale(0, RoundingMode.CEILING).intValue();
        if (lowerIndex == upperIndex) {
            return sorted.get(lowerIndex).setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal fraction = position.subtract(BigDecimal.valueOf(lowerIndex));
        return sorted.get(lowerIndex)
            .add(
                sorted.get(upperIndex)
                    .subtract(sorted.get(lowerIndex))
                    .multiply(fraction)
            )
            .setScale(2, RoundingMode.HALF_UP);
    }
}
