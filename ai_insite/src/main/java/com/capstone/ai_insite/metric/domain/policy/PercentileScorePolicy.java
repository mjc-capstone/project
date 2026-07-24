package com.capstone.ai_insite.metric.domain.policy;

import com.capstone.ai_insite.common.value.ScoreMath;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PercentileScorePolicy {

    public <K> Map<K, BigDecimal> score(Map<K, BigDecimal> values) {
        List<BigDecimal> sorted = values.values().stream()
            .filter(Objects::nonNull)
            .sorted()
            .toList();
        Map<K, BigDecimal> result = new LinkedHashMap<>();
        values.forEach((key, value) -> {
            if (value != null) {
                result.put(key, percentile(value, sorted));
            }
        });
        return result;
    }

    private static BigDecimal percentile(
        BigDecimal value,
        List<BigDecimal> sorted
    ) {
        if (sorted.size() == 1) {
            return ScoreMath.NEUTRAL.setScale(4, RoundingMode.HALF_UP);
        }
        List<Integer> matchingIndexes = new ArrayList<>();
        for (int index = 0; index < sorted.size(); index++) {
            if (sorted.get(index).compareTo(value) == 0) {
                matchingIndexes.add(index);
            }
        }
        BigDecimal averageIndex = matchingIndexes.stream()
            .map(BigDecimal::valueOf)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(
                BigDecimal.valueOf(matchingIndexes.size()),
                8,
                RoundingMode.HALF_UP
            );
        return ScoreMath.clamp(
            averageIndex.multiply(BigDecimal.valueOf(100))
                .divide(
                    BigDecimal.valueOf(sorted.size() - 1L),
                    8,
                    RoundingMode.HALF_UP
                )
        );
    }
}
