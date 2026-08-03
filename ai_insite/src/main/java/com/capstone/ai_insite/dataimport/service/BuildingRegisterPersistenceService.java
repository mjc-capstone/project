package com.capstone.ai_insite.dataimport.service;

import com.capstone.ai_insite.dataimport.dto.publicdata.CollectedBuildingRegister;
import com.capstone.ai_insite.metric.entity.BuildingRegionMappingEntity;
import com.capstone.ai_insite.metric.entity.SourceMolitBuildingRegisterEntity;
import com.capstone.ai_insite.metric.repository.BuildingRegionMappingJpaRepository;
import com.capstone.ai_insite.metric.repository.SourceMolitBuildingRegisterJpaRepository;
import com.capstone.ai_insite.region.entity.AdministrativeLegalDongMappingEntity;
import com.capstone.ai_insite.region.entity.LegalDongEntity;
import com.capstone.ai_insite.region.entity.RegionEntity;
import com.capstone.ai_insite.region.repository.AdministrativeLegalDongMappingJpaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BuildingRegisterPersistenceService {

    private final SourceMolitBuildingRegisterJpaRepository sourceRepository;
    private final BuildingRegionMappingJpaRepository buildingMappingRepository;
    private final AdministrativeLegalDongMappingJpaRepository regionMappingRepository;

    public BuildingRegisterPersistenceService(
        SourceMolitBuildingRegisterJpaRepository sourceRepository,
        BuildingRegionMappingJpaRepository buildingMappingRepository,
        AdministrativeLegalDongMappingJpaRepository regionMappingRepository
    ) {
        this.sourceRepository = sourceRepository;
        this.buildingMappingRepository = buildingMappingRepository;
        this.regionMappingRepository = regionMappingRepository;
    }

    @Transactional
    public int replace(
        LegalDongEntity legalDong,
        LocalDate snapshotDate,
        List<CollectedBuildingRegister> collected
    ) {
        buildingMappingRepository
            .deleteBySourceBuildingLegalDongIdAndSourceBuildingSnapshotDate(
                legalDong.getId(),
                snapshotDate
            );
        sourceRepository.deleteByLegalDongIdAndSnapshotDate(
            legalDong.getId(),
            snapshotDate
        );
        buildingMappingRepository.flush();
        sourceRepository.flush();

        Map<String, CollectedBuildingRegister> unique = collected.stream()
            .collect(Collectors.toMap(
                value -> value.row().buildingRegisterId(),
                Function.identity(),
                (left, right) -> right,
                LinkedHashMap::new
            ));
        List<SourceMolitBuildingRegisterEntity> sources = sourceRepository.saveAll(
            unique.values().stream()
                .map(value -> new SourceMolitBuildingRegisterEntity(
                    value.rawApiPayload(),
                    legalDong,
                    snapshotDate,
                    value.row()
                ))
                .toList()
        );
        sourceRepository.flush();
        MappingDecision decision = decision(legalDong);
        buildingMappingRepository.saveAll(sources.stream()
            .map(source -> new BuildingRegionMappingEntity(
                source,
                decision.region(),
                decision.status(),
                decision.confidence(),
                decision.rule(),
                decision.candidateCount()
            ))
            .toList());
        return sources.size();
    }

    private MappingDecision decision(LegalDongEntity legalDong) {
        List<AdministrativeLegalDongMappingEntity> usable =
            regionMappingRepository.findByLegalDongIdIn(List.of(legalDong.getId()))
                .stream()
                .filter(AdministrativeLegalDongMappingEntity::isUsable)
                .toList();
        Map<Long, RegionEntity> regions = usable.stream()
            .collect(Collectors.toMap(
                mapping -> mapping.getRegion().getId(),
                AdministrativeLegalDongMappingEntity::getRegion,
                (left, right) -> left,
                LinkedHashMap::new
            ));
        if (regions.size() == 1) {
            BigDecimal confidence = usable.stream()
                .map(AdministrativeLegalDongMappingEntity::getMappingConfidence)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ONE);
            return new MappingDecision(
                regions.values().iterator().next(),
                "CONFIRMED",
                confidence,
                "UNIQUE_USABLE_LEGAL_DONG_MAPPING",
                1
            );
        }
        if (regions.size() > 1) {
            return new MappingDecision(
                null,
                "AMBIGUOUS",
                BigDecimal.ZERO.setScale(4),
                "MULTIPLE_USABLE_LEGAL_DONG_MAPPINGS",
                regions.size()
            );
        }
        return new MappingDecision(
            null,
            "UNMAPPED",
            BigDecimal.ZERO.setScale(4),
            "NO_USABLE_LEGAL_DONG_MAPPING",
            0
        );
    }

    private record MappingDecision(
        RegionEntity region,
        String status,
        BigDecimal confidence,
        String rule,
        int candidateCount
    ) {
    }
}
