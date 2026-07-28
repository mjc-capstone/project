package com.capstone.ai_insite.category.domain.policy;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class CategoryMappingPolicy {

    private static final Map<String, String> KEYWORD_RULES = Map.ofEntries(
        Map.entry("한식", "한식"),
        Map.entry("중식", "중식"),
        Map.entry("일식", "일식"),
        Map.entry("커피", "커피"),
        Map.entry("카페", "커피"),
        Map.entry("치킨", "치킨"),
        Map.entry("피자", "피자"),
        Map.entry("제과", "제과"),
        Map.entry("빵", "제과"),
        Map.entry("패스트푸드", "패스트푸드"),
        Map.entry("햄버거", "패스트푸드"),
        Map.entry("호프", "호프"),
        Map.entry("주점", "호프"),
        Map.entry("치과", "치과"),
        Map.entry("한의", "한의"),
        Map.entry("약국", "의약품"),
        Map.entry("편의점", "편의점"),
        Map.entry("슈퍼", "슈퍼"),
        Map.entry("미용", "미용"),
        Map.entry("세탁", "세탁"),
        Map.entry("자동차수리", "자동차수리")
    );

    public Optional<Decision> resolve(
        String sourceSmallCategoryName,
        List<ExplicitCandidate> explicitCandidates,
        List<NameCandidate> nameCandidates
    ) {
        Optional<ExplicitCandidate> explicit = explicitCandidates.stream()
            .max(Comparator.comparing(ExplicitCandidate::confidence));
        if (explicit.isPresent()) {
            ExplicitCandidate match = explicit.get();
            return Optional.of(new Decision(
                match.categoryId(),
                match.confidence(),
                match.rule()
            ));
        }
        String normalizedSource = normalize(sourceSmallCategoryName);
        Optional<NameCandidate> exact = nameCandidates.stream()
            .filter(candidate -> normalize(candidate.categoryName())
                .equals(normalizedSource))
            .findFirst();
        if (exact.isPresent()) {
            return Optional.of(new Decision(
                exact.get().categoryId(),
                new BigDecimal("0.9000"),
                "EXACT_NORMALIZED_NAME"
            ));
        }
        for (var rule : KEYWORD_RULES.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .toList()) {
            if (normalizedSource.contains(normalize(rule.getKey()))) {
                Optional<NameCandidate> target = nameCandidates.stream()
                    .filter(candidate -> normalize(candidate.categoryName())
                        .contains(normalize(rule.getValue())))
                    .findFirst();
                if (target.isPresent()) {
                    return Optional.of(new Decision(
                        target.get().categoryId(),
                        new BigDecimal("0.8000"),
                        "CONTROLLED_KEYWORD:" + rule.getKey()
                    ));
                }
            }
        }
        return Optional.empty();
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
            .replaceAll("[^0-9a-z가-힣]", "")
            .replace("음식점업", "")
            .replace("음식점", "")
            .replace("전문점", "");
    }

    public record ExplicitCandidate(
        Long categoryId,
        BigDecimal confidence,
        String rule
    ) {
    }

    public record NameCandidate(Long categoryId, String categoryName) {
    }

    public record Decision(
        Long categoryId,
        BigDecimal confidence,
        String rule
    ) {
    }
}
