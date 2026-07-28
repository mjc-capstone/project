package com.capstone.ai_insite.category.service;

import com.capstone.ai_insite.category.domain.policy.CategoryMappingPolicy;
import com.capstone.ai_insite.category.entity.BusinessCategoryEntity;
import com.capstone.ai_insite.category.entity.CategoryCodeMappingEntity;
import com.capstone.ai_insite.category.repository.BusinessCategoryJpaRepository;
import com.capstone.ai_insite.category.repository.CategoryCodeMappingJpaRepository;
import com.capstone.ai_insite.dataimport.domain.SmallBusinessStoreImportCommand;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryMappingApplicationService {

    private final BusinessCategoryJpaRepository categoryRepository;
    private final CategoryCodeMappingJpaRepository codeMappingRepository;
    private final CategoryMappingPolicy policy;

    public CategoryMappingApplicationService(
        BusinessCategoryJpaRepository categoryRepository,
        CategoryCodeMappingJpaRepository codeMappingRepository,
        CategoryMappingPolicy policy
    ) {
        this.categoryRepository = categoryRepository;
        this.codeMappingRepository = codeMappingRepository;
        this.policy = policy;
    }

    @Transactional(readOnly = true)
    public Map<String, Resolution> resolveBatch(
        List<SmallBusinessStoreImportCommand> commands
    ) {
        List<BusinessCategoryEntity> categories = categoryRepository.findByActiveTrue();
        categories = categories.stream()
            .sorted(Comparator.comparing(BusinessCategoryEntity::getId))
            .toList();
        Map<Long, BusinessCategoryEntity> categoriesById = categories.stream()
            .collect(Collectors.toMap(BusinessCategoryEntity::getId, Function.identity()));
        Map<String, BusinessCategoryEntity> categoriesByNormalizedCode = categories.stream()
            .filter(category -> category.getNormalizedCategoryCode() != null)
            .collect(Collectors.toMap(
                BusinessCategoryEntity::getNormalizedCategoryCode,
                Function.identity(),
                (first, ignored) -> first
            ));

        List<CategoryCodeMappingEntity> mappings = loadMappings(commands);
        Map<String, List<CategoryCodeMappingEntity>> mappingsBySmallCode =
            groupBy(mappings, CategoryCodeMappingEntity::getSmallBusinessCategoryCode);
        Map<String, List<CategoryCodeMappingEntity>> mappingsByKsic =
            groupBy(mappings, CategoryCodeMappingEntity::getKsicCode);
        Map<String, Resolution> resolved = new HashMap<>();
        for (SmallBusinessStoreImportCommand command : commands) {
            List<CategoryMappingPolicy.ExplicitCandidate> explicit =
                explicitCandidates(
                    command,
                    mappingsBySmallCode,
                    mappingsByKsic,
                    categoriesByNormalizedCode
                );
            policy.resolve(command.sourceSmallCategoryName(), explicit, List.of())
                .map(decision -> new Resolution(
                    categoriesById.get(decision.categoryId()),
                    decision.confidence(),
                    decision.rule()
                ))
                .filter(resolution -> resolution.category() != null)
                .ifPresent(resolution -> resolved.put(
                    command.externalStoreId(),
                    resolution
                ));
        }
        return Map.copyOf(resolved);
    }

    private List<CategoryCodeMappingEntity> loadMappings(
        List<SmallBusinessStoreImportCommand> commands
    ) {
        Collection<String> smallCodes = distinctValues(
            commands,
            SmallBusinessStoreImportCommand::sourceSmallCategoryCode
        );
        Collection<String> ksicCodes = distinctValues(
            commands,
            SmallBusinessStoreImportCommand::ksicCode
        );
        Map<Long, CategoryCodeMappingEntity> mappings = new HashMap<>();
        if (!smallCodes.isEmpty()) {
            codeMappingRepository.findBySmallBusinessCategoryCodeIn(smallCodes)
                .forEach(mapping -> mappings.put(mapping.getId(), mapping));
        }
        if (!ksicCodes.isEmpty()) {
            codeMappingRepository.findByKsicCodeIn(ksicCodes)
                .forEach(mapping -> mappings.put(mapping.getId(), mapping));
        }
        return mappings.values().stream()
            .filter(CategoryCodeMappingEntity::isConfirmed)
            .toList();
    }

    private static List<CategoryMappingPolicy.ExplicitCandidate> explicitCandidates(
        SmallBusinessStoreImportCommand command,
        Map<String, List<CategoryCodeMappingEntity>> mappingsBySmallCode,
        Map<String, List<CategoryCodeMappingEntity>> mappingsByKsic,
        Map<String, BusinessCategoryEntity> categoriesByNormalizedCode
    ) {
        return java.util.stream.Stream.concat(
                mappingsBySmallCode.getOrDefault(
                    command.sourceSmallCategoryCode(),
                    List.of()
                ).stream(),
                mappingsByKsic.getOrDefault(command.ksicCode(), List.of()).stream()
            )
            .distinct()
            .map(mapping -> {
                BusinessCategoryEntity category = categoriesByNormalizedCode.get(
                    mapping.getNormalizedCategoryCode()
                );
                return category == null
                    ? null
                    : new CategoryMappingPolicy.ExplicitCandidate(
                        category.getId(),
                        mapping.getMappingConfidence(),
                        mapping.getMappingRule()
                    );
            })
            .filter(Objects::nonNull)
            .toList();
    }

    private static Map<String, List<CategoryCodeMappingEntity>> groupBy(
        List<CategoryCodeMappingEntity> mappings,
        Function<CategoryCodeMappingEntity, String> classifier
    ) {
        return mappings.stream()
            .filter(mapping -> classifier.apply(mapping) != null)
            .collect(Collectors.groupingBy(classifier));
    }

    private static Collection<String> distinctValues(
        List<SmallBusinessStoreImportCommand> commands,
        Function<SmallBusinessStoreImportCommand, String> extractor
    ) {
        return commands.stream()
            .map(extractor)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    public record Resolution(
        BusinessCategoryEntity category,
        BigDecimal confidence,
        String rule
    ) {
    }
}
