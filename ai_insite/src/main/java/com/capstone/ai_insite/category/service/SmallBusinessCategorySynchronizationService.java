package com.capstone.ai_insite.category.service;

import com.capstone.ai_insite.category.entity.SmallBusinessCategoryEntity;
import com.capstone.ai_insite.category.repository.SmallBusinessCategoryJpaRepository;
import com.capstone.ai_insite.dataimport.dto.publicdata.SmallBusinessCategoryRow;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SmallBusinessCategorySynchronizationService {

    private final SmallBusinessCategoryJpaRepository repository;

    public SmallBusinessCategorySynchronizationService(
        SmallBusinessCategoryJpaRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional
    public int synchronize(List<SmallBusinessCategoryRow> rows) {
        Set<String> sourceCodes = rows.stream()
            .map(SmallBusinessCategoryRow::smallCategoryCode)
            .collect(Collectors.toSet());
        Map<String, SmallBusinessCategoryEntity> existing = repository
            .findBySmallCategoryCodeIn(sourceCodes)
            .stream()
            .collect(Collectors.toMap(
                SmallBusinessCategoryEntity::getSmallCategoryCode,
                Function.identity()
            ));
        List<SmallBusinessCategoryEntity> changed = rows.stream().map(row -> {
            SmallBusinessCategoryEntity entity = existing.getOrDefault(
                row.smallCategoryCode(),
                SmallBusinessCategoryEntity.create(row.smallCategoryCode())
            );
            entity.synchronize(
                row.largeCategoryCode(),
                row.largeCategoryName(),
                row.mediumCategoryCode(),
                row.mediumCategoryName(),
                row.smallCategoryCode(),
                row.smallCategoryName(),
                row.sourceReferenceDate()
            );
            return entity;
        }).toList();
        repository.saveAll(changed);
        repository.findByActiveTrue().stream()
            .filter(entity -> !sourceCodes.contains(entity.getSmallCategoryCode()))
            .forEach(SmallBusinessCategoryEntity::deactivate);
        return changed.size();
    }
}
