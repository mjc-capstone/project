package com.capstone.ai_insite.dataimport.service;

import com.capstone.ai_insite.category.service.CategoryMappingApplicationService;
import com.capstone.ai_insite.common.exception.ResourceNotFoundException;
import com.capstone.ai_insite.dataimport.domain.SmallBusinessStoreBatchResult;
import com.capstone.ai_insite.dataimport.domain.SmallBusinessStoreImportCommand;
import com.capstone.ai_insite.dataimport.entity.SourceSmallBusinessStoreEntity;
import com.capstone.ai_insite.dataimport.repository.RawApiPayloadJpaRepository;
import com.capstone.ai_insite.dataimport.repository.SourceSmallBusinessStoreJpaRepository;
import com.capstone.ai_insite.region.service.StoreRegionMappingApplicationService;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SmallBusinessStoreImportService {

    private final RawApiPayloadJpaRepository rawPayloadRepository;
    private final SourceSmallBusinessStoreJpaRepository storeRepository;
    private final CategoryMappingApplicationService categoryMappingService;
    private final StoreRegionMappingApplicationService regionMappingService;

    public SmallBusinessStoreImportService(
        RawApiPayloadJpaRepository rawPayloadRepository,
        SourceSmallBusinessStoreJpaRepository storeRepository,
        CategoryMappingApplicationService categoryMappingService,
        StoreRegionMappingApplicationService regionMappingService
    ) {
        this.rawPayloadRepository = rawPayloadRepository;
        this.storeRepository = storeRepository;
        this.categoryMappingService = categoryMappingService;
        this.regionMappingService = regionMappingService;
    }

    @Transactional
    public SmallBusinessStoreBatchResult importBatch(
        Long rawPayloadId,
        List<SmallBusinessStoreImportCommand> commands
    ) {
        if (commands.isEmpty()) {
            return new SmallBusinessStoreBatchResult(0, 0, 0);
        }
        LocalDate snapshotDate = singleSnapshotDate(commands);
        var rawPayload = rawPayloadRepository.findById(rawPayloadId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Raw API payload not found: " + rawPayloadId
            ));
        Map<String, CategoryMappingApplicationService.Resolution> categories =
            categoryMappingService.resolveBatch(commands);
        Map<String, StoreRegionMappingApplicationService.Resolution> regions =
            regionMappingService.resolveBatch(commands);
        Map<String, SourceSmallBusinessStoreEntity> existingByExternalId =
            storeRepository.findByExternalStoreIdInAndSnapshotDate(
                commands.stream()
                    .map(SmallBusinessStoreImportCommand::externalStoreId)
                    .toList(),
                snapshotDate
            ).stream().collect(Collectors.toMap(
                SourceSmallBusinessStoreEntity::getExternalStoreId,
                Function.identity()
            ));

        Map<String, SourceSmallBusinessStoreEntity> changed = new HashMap<>();
        int regionMapped = 0;
        int categoryMapped = 0;
        for (SmallBusinessStoreImportCommand command : commands) {
            var category = categories.get(command.externalStoreId());
            var region = regions.get(command.externalStoreId());
            SourceSmallBusinessStoreEntity entity = existingByExternalId.getOrDefault(
                command.externalStoreId(),
                new SourceSmallBusinessStoreEntity(
                    command.externalStoreId(),
                    command.snapshotDate()
                )
            );
            entity.apply(
                command,
                rawPayload,
                region == null ? null : region.region(),
                region == null ? null : region.legalDong(),
                category == null ? null : category.category(),
                region == null ? null : region.confidence(),
                region == null ? null : region.rule(),
                category == null ? null : category.confidence(),
                category == null ? null : category.rule()
            );
            changed.put(command.externalStoreId(), entity);
            regionMapped += region != null && region.region() != null ? 1 : 0;
            categoryMapped += category != null ? 1 : 0;
        }
        storeRepository.saveAll(changed.values());
        return new SmallBusinessStoreBatchResult(
            changed.size(),
            regionMapped,
            categoryMapped
        );
    }

    private static LocalDate singleSnapshotDate(
        List<SmallBusinessStoreImportCommand> commands
    ) {
        LocalDate first = commands.getFirst().snapshotDate();
        boolean mixed = commands.stream()
            .anyMatch(command -> !first.equals(command.snapshotDate()));
        if (mixed) {
            throw new IllegalArgumentException(
                "A small-business store batch must have one snapshot date."
            );
        }
        return first;
    }
}
