package com.capstone.ai_insite.dataimport.service;

import com.capstone.ai_insite.dataimport.domain.SeoulRegionalImportCommand;
import com.capstone.ai_insite.dataimport.entity.RawApiPayloadEntity;
import com.capstone.ai_insite.dataimport.repository.RawApiPayloadJpaRepository;
import com.capstone.ai_insite.metric.entity.AbstractSeoulRegionalSourceEntity;
import com.capstone.ai_insite.metric.entity.MetricPeriodEntity;
import com.capstone.ai_insite.metric.entity.SourceSeoulApartmentsEntity;
import com.capstone.ai_insite.metric.entity.SourceSeoulFacilitiesEntity;
import com.capstone.ai_insite.metric.entity.SourceSeoulFloatingPopulationEntity;
import com.capstone.ai_insite.metric.entity.SourceSeoulResidentPopulationEntity;
import com.capstone.ai_insite.metric.entity.SourceSeoulWorkingPopulationEntity;
import com.capstone.ai_insite.metric.repository.SeoulRegionalSourceJpaRepository;
import com.capstone.ai_insite.metric.repository.SourceSeoulApartmentsJpaRepository;
import com.capstone.ai_insite.metric.repository.SourceSeoulFacilitiesJpaRepository;
import com.capstone.ai_insite.metric.repository.SourceSeoulFloatingPopulationJpaRepository;
import com.capstone.ai_insite.metric.repository.SourceSeoulResidentPopulationJpaRepository;
import com.capstone.ai_insite.metric.repository.SourceSeoulWorkingPopulationJpaRepository;
import com.capstone.ai_insite.region.entity.RegionEntity;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeoulRegionalSourceImportService {

    private final RawApiPayloadJpaRepository rawPayloadRepository;
    private final SeoulMasterDataService masterDataService;
    private final SourceSeoulFloatingPopulationJpaRepository floatingRepository;
    private final SourceSeoulResidentPopulationJpaRepository residentRepository;
    private final SourceSeoulWorkingPopulationJpaRepository workingRepository;
    private final SourceSeoulFacilitiesJpaRepository facilitiesRepository;
    private final SourceSeoulApartmentsJpaRepository apartmentsRepository;

    public SeoulRegionalSourceImportService(
        RawApiPayloadJpaRepository rawPayloadRepository,
        SeoulMasterDataService masterDataService,
        SourceSeoulFloatingPopulationJpaRepository floatingRepository,
        SourceSeoulResidentPopulationJpaRepository residentRepository,
        SourceSeoulWorkingPopulationJpaRepository workingRepository,
        SourceSeoulFacilitiesJpaRepository facilitiesRepository,
        SourceSeoulApartmentsJpaRepository apartmentsRepository
    ) {
        this.rawPayloadRepository = rawPayloadRepository;
        this.masterDataService = masterDataService;
        this.floatingRepository = floatingRepository;
        this.residentRepository = residentRepository;
        this.workingRepository = workingRepository;
        this.facilitiesRepository = facilitiesRepository;
        this.apartmentsRepository = apartmentsRepository;
    }

    @Transactional
    public int importFloatingPopulation(
        Long rawPayloadId,
        List<SeoulRegionalImportCommand.FloatingPopulation> commands
    ) {
        return importBatch(
            rawPayloadId,
            commands,
            floatingRepository,
            SourceSeoulFloatingPopulationEntity::new
        );
    }

    @Transactional
    public int importResidentPopulation(
        Long rawPayloadId,
        List<SeoulRegionalImportCommand.ResidentPopulation> commands
    ) {
        return importBatch(
            rawPayloadId,
            commands,
            residentRepository,
            SourceSeoulResidentPopulationEntity::new
        );
    }

    @Transactional
    public int importWorkingPopulation(
        Long rawPayloadId,
        List<SeoulRegionalImportCommand.WorkingPopulation> commands
    ) {
        return importBatch(
            rawPayloadId,
            commands,
            workingRepository,
            SourceSeoulWorkingPopulationEntity::new
        );
    }

    @Transactional
    public int importFacilities(
        Long rawPayloadId,
        List<SeoulRegionalImportCommand.Facilities> commands
    ) {
        return importBatch(
            rawPayloadId,
            commands,
            facilitiesRepository,
            SourceSeoulFacilitiesEntity::new
        );
    }

    @Transactional
    public int importApartments(
        Long rawPayloadId,
        List<SeoulRegionalImportCommand.Apartments> commands
    ) {
        return importBatch(
            rawPayloadId,
            commands,
            apartmentsRepository,
            SourceSeoulApartmentsEntity::new
        );
    }

    private <C extends SeoulRegionalImportCommand,
        E extends AbstractSeoulRegionalSourceEntity<C>> int importBatch(
        Long rawPayloadId,
        List<C> commands,
        SeoulRegionalSourceJpaRepository<E> repository,
        EntityFactory<E> entityFactory
    ) {
        if (commands.isEmpty()) {
            return 0;
        }
        RawApiPayloadEntity rawPayload = rawPayloadRepository.findById(rawPayloadId)
            .orElseThrow(() -> new IllegalArgumentException(
                "원본 API 응답을 찾을 수 없습니다: " + rawPayloadId
            ));
        SeoulMasterSnapshot masters = masterDataService.synchronizeRegionPeriods(commands);
        Set<String> periodCodes = commands.stream()
            .map(SeoulRegionalImportCommand::periodCode)
            .collect(Collectors.toSet());
        if (periodCodes.size() != 1) {
            throw new IllegalArgumentException("한 정규화 배치에는 하나의 분기만 포함되어야 합니다.");
        }
        MetricPeriodEntity period = masters.period(periodCodes.iterator().next());
        Set<Long> regionIds = commands.stream()
            .map(command -> masters.region(command.regionCode()).getId())
            .collect(Collectors.toSet());
        Map<Long, E> entities = repository
            .findByMetricPeriodIdAndRegionIdIn(period.getId(), regionIds)
            .stream()
            .collect(Collectors.toMap(
                entity -> entity.getRegion().getId(),
                entity -> entity,
                (first, ignored) -> first,
                LinkedHashMap::new
            ));
        for (C command : commands) {
            RegionEntity region = masters.region(command.regionCode());
            E entity = entities.computeIfAbsent(
                region.getId(),
                ignored -> entityFactory.create(rawPayload, region, period)
            );
            entity.apply(command, rawPayload);
        }
        repository.saveAll(entities.values());
        return commands.size();
    }

    @FunctionalInterface
    private interface EntityFactory<E> {

        E create(
            RawApiPayloadEntity rawPayload,
            RegionEntity region,
            MetricPeriodEntity period
        );
    }
}
