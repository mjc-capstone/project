package com.capstone.ai_insite.metric.domain.policy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class CompetitionDensityPolicy {

    public BigDecimal categoryDiversityIndex(List<Long> categoryStoreCounts) {
        List<Long> positiveCounts = categoryStoreCounts.stream()
            .filter(count -> count != null && count > 0)
            .toList();
        if (positiveCounts.size() <= 1) {
            return BigDecimal.ZERO.setScale(6);
        }
        double total = positiveCounts.stream().mapToLong(Long::longValue).sum();
        double entropy = positiveCounts.stream()
            .mapToDouble(count -> {
                double probability = count / total;
                return -probability * Math.log(probability);
            })
            .sum();
        double normalized = entropy / Math.log(positiveCounts.size()) * 100.0;
        return BigDecimal.valueOf(normalized)
            .setScale(6, RoundingMode.HALF_UP);
    }
}
