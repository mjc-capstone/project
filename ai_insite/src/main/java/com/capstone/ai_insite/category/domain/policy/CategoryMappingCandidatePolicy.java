package com.capstone.ai_insite.category.domain.policy;

import com.capstone.ai_insite.category.domain.MappingStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CategoryMappingCandidatePolicy {

    public Decision propose(
        String sourceName,
        List<Target> targets
    ) {
        String source = normalize(sourceName);
        if (source.isEmpty()) {
            return Decision.unresolved();
        }
        for (Target target : targets) {
            if (source.equals(normalize(target.name()))) {
                return new Decision(
                    target.categoryId(),
                    MappingStatus.AUTO_CONFIRMED,
                    new BigDecimal("0.9500"),
                    "EXACT_CANONICAL_NAME"
                );
            }
        }
        Target best = null;
        double bestScore = 0;
        for (Target target : targets) {
            double score = dice(source, normalize(target.name()));
            if (score > bestScore) {
                best = target;
                bestScore = score;
            }
        }
        if (best == null || bestScore < 0.45) {
            return Decision.unresolved();
        }
        BigDecimal confidence = BigDecimal.valueOf(
            Math.min(0.85, 0.35 + bestScore * 0.5)
        ).setScale(4, RoundingMode.HALF_UP);
        return new Decision(
            best.categoryId(),
            MappingStatus.CANDIDATE,
            confidence,
            "BIGRAM_DICE_CANDIDATE"
        );
    }

    private static String normalize(String value) {
        return value == null
            ? ""
            : value.toLowerCase(Locale.ROOT)
                .replaceAll("[^0-9a-z가-힣]", "");
    }

    private static double dice(String left, String right) {
        if (left.equals(right)) {
            return 1.0;
        }
        Set<String> leftBigrams = bigrams(left);
        Set<String> rightBigrams = bigrams(right);
        if (leftBigrams.isEmpty() || rightBigrams.isEmpty()) {
            return 0;
        }
        long intersection = leftBigrams.stream()
            .filter(rightBigrams::contains)
            .count();
        return 2.0 * intersection
            / (leftBigrams.size() + rightBigrams.size());
    }

    private static Set<String> bigrams(String value) {
        Set<String> result = new HashSet<>();
        for (int index = 0; index < value.length() - 1; index++) {
            result.add(value.substring(index, index + 2));
        }
        return result;
    }

    public record Target(Long categoryId, String name) {
    }

    public record Decision(
        Long categoryId,
        MappingStatus status,
        BigDecimal confidence,
        String rule
    ) {
        static Decision unresolved() {
            return new Decision(
                null,
                MappingStatus.UNRESOLVED,
                null,
                "NO_RELIABLE_CANDIDATE"
            );
        }
    }
}
