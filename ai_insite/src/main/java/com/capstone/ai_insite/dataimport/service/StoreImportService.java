package com.capstone.ai_insite.dataimport.service;

import com.capstone.ai_insite.category.entity.BusinessCategoryEntity;
import com.capstone.ai_insite.dataimport.domain.StoreImportCommand;
import com.capstone.ai_insite.dataimport.domain.SeoulMasterReference;
import com.capstone.ai_insite.dataimport.entity.RawApiPayloadEntity;
import com.capstone.ai_insite.dataimport.repository.RawApiPayloadJpaRepository;
import com.capstone.ai_insite.metric.entity.MetricPeriodEntity;
import com.capstone.ai_insite.metric.entity.SourceSeoulStoresEntity;
import com.capstone.ai_insite.metric.repository.SourceSeoulStoresJpaRepository;
import com.capstone.ai_insite.region.entity.RegionEntity;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreImportService {

    private final RawPayloadService rawPayloadService;
    private final ImportMasterResolver masterResolver;
    private final SourceSeoulStoresJpaRepository storesRepository;
    private final RawApiPayloadJpaRepository rawPayloadRepository;
    private final SeoulMasterDataService masterDataService;

    public StoreImportService(
        RawPayloadService rawPayloadService,
        ImportMasterResolver masterResolver,
        SourceSeoulStoresJpaRepository storesRepository,
        RawApiPayloadJpaRepository rawPayloadRepository,
        SeoulMasterDataService masterDataService
    ) {
        this.rawPayloadService = rawPayloadService;
        this.masterResolver = masterResolver;
        this.storesRepository = storesRepository;
        this.rawPayloadRepository = rawPayloadRepository;
        this.masterDataService = masterDataService;
    }

    @Transactional
    public Long importStores(StoreImportCommand command) {
        RawApiPayloadEntity rawPayload = rawPayloadService.save(command.rawPayload());
        RegionEntity region = masterResolver.region(command.regionCode());
        BusinessCategoryEntity category = masterResolver.category(command.categoryCode());
        MetricPeriodEntity period = masterResolver.period(command.periodCode());

        SourceSeoulStoresEntity stores = storesRepository
            .findByRegionIdAndBusinessCategoryIdAndMetricPeriodId(
                region.getId(),
                category.getId(),
                period.getId()
            )
            .orElseGet(() -> new SourceSeoulStoresEntity(rawPayload, region, category, period));
        stores.apply(command, rawPayload);
        return storesRepository.save(stores).getId();
    }

    @Transactional
    public int importBatch(Long rawPayloadId, List<StoreImportCommand> commands) {
        if (commands.isEmpty()) {
            return 0;
        }
        RawApiPayloadEntity rawPayload = rawPayloadRepository.findById(rawPayloadId)
            .orElseThrow(() -> new IllegalArgumentException(
                "원본 API 응답을 찾을 수 없습니다: " + rawPayloadId
            ));
        SeoulMasterSnapshot masters = masterDataService.synchronize(
            commands.stream().map(StoreImportService::masterReference).toList()
        );
        MetricPeriodEntity period = singlePeriod(commands, masters);
        Set<Long> regionIds = commands.stream()
            .map(command -> masters.region(command.regionCode()).getId())
            .collect(Collectors.toSet());
        Map<SourceKey, SourceSeoulStoresEntity> existing = storesRepository
            .findByMetricPeriodIdAndRegionIdIn(period.getId(), regionIds)
            .stream()
            .collect(Collectors.toMap(
                entity -> new SourceKey(
                    entity.getRegion().getId(),
                    entity.getBusinessCategory().getId()
                ),
                entity -> entity,
                (first, ignored) -> first,
                LinkedHashMap::new
            ));

        for (StoreImportCommand command : commands) {
            RegionEntity region = masters.region(command.regionCode());
            BusinessCategoryEntity category = masters.category(command.categoryCode());
            SourceKey key = new SourceKey(region.getId(), category.getId());
            SourceSeoulStoresEntity entity = existing.computeIfAbsent(
                key,
                ignored -> new SourceSeoulStoresEntity(rawPayload, region, category, period)
            );
            entity.apply(command, rawPayload);
        }
        storesRepository.saveAll(existing.values());
        return commands.size();
    }

    private static SeoulMasterReference masterReference(StoreImportCommand command) {
        return new SeoulMasterReference(
            command.sourcePeriodCode(),
            command.regionCode(),
            command.regionName(),
            command.categoryCode(),
            command.categoryName()
        );
    }

    private static MetricPeriodEntity singlePeriod(
        Collection<StoreImportCommand> commands,
        SeoulMasterSnapshot masters
    ) {
        Set<String> periods = commands.stream()
            .map(StoreImportCommand::periodCode)
            .collect(Collectors.toSet());
        if (periods.size() != 1) {
            throw new IllegalArgumentException("한 페이지에는 하나의 분기만 포함되어야 합니다.");
        }
        return masters.period(periods.iterator().next());
    }

    private record SourceKey(Long regionId, Long categoryId) {
    }
}
