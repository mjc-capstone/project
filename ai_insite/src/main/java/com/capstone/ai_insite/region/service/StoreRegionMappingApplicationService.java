package com.capstone.ai_insite.region.service;

import com.capstone.ai_insite.dataimport.domain.SmallBusinessStoreImportCommand;
import com.capstone.ai_insite.region.domain.policy.StoreRegionMappingPolicy;
import com.capstone.ai_insite.region.entity.AdministrativeLegalDongMappingEntity;
import com.capstone.ai_insite.region.entity.LegalDongEntity;
import com.capstone.ai_insite.region.entity.RegionEntity;
import com.capstone.ai_insite.region.repository.AdministrativeLegalDongMappingJpaRepository;
import com.capstone.ai_insite.region.repository.LegalDongJpaRepository;
import com.capstone.ai_insite.region.repository.RegionJpaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreRegionMappingApplicationService {

    private final RegionJpaRepository regionRepository;
    private final LegalDongJpaRepository legalDongRepository;
    private final AdministrativeLegalDongMappingJpaRepository mappingRepository;
    private final StoreRegionMappingPolicy policy;

    public StoreRegionMappingApplicationService(
        RegionJpaRepository regionRepository,
        LegalDongJpaRepository legalDongRepository,
        AdministrativeLegalDongMappingJpaRepository mappingRepository,
        StoreRegionMappingPolicy policy
    ) {
        this.regionRepository = regionRepository;
        this.legalDongRepository = legalDongRepository;
        this.mappingRepository = mappingRepository;
        this.policy = policy;
    }

    @Transactional(readOnly = true)
    public Map<String, Resolution> resolveBatch(
        List<SmallBusinessStoreImportCommand> commands
    ) {
        Map<String, RegionEntity> regionsByCode = regionRepository
            .findByAdministrativeDongCodeIn(distinctAdminCodes(commands))
            .stream()
            .filter(RegionEntity::isActive)
            .collect(Collectors.toMap(
                RegionEntity::getAdministrativeDongCode,
                Function.identity()
            ));
        Map<String, LegalDongEntity> legalDongsByCode = legalDongRepository
            .findByLegalDongCodeIn(distinctLegalCodes(commands))
            .stream()
            .filter(LegalDongEntity::isActive)
            .collect(Collectors.toMap(
                LegalDongEntity::getLegalDongCode,
                Function.identity()
            ));
        List<Long> legalDongIds = legalDongsByCode.values().stream()
            .map(LegalDongEntity::getId)
            .toList();
        Map<Long, List<AdministrativeLegalDongMappingEntity>> mappingsByLegalId =
            legalDongIds.isEmpty()
                ? Map.of()
                : mappingRepository.findByLegalDongIdIn(legalDongIds).stream()
                    .collect(Collectors.groupingBy(
                        mapping -> mapping.getLegalDong().getId()
                    ));
        Map<Long, RegionEntity> regionsById = new HashMap<>();
        regionsByCode.values().forEach(region -> regionsById.put(region.getId(), region));
        mappingsByLegalId.values().stream()
            .flatMap(List::stream)
            .map(AdministrativeLegalDongMappingEntity::getRegion)
            .forEach(region -> regionsById.put(region.getId(), region));

        Map<String, Resolution> resolved = new HashMap<>();
        for (SmallBusinessStoreImportCommand command : commands) {
            RegionEntity directRegion = regionsByCode.get(command.administrativeDongCode());
            LegalDongEntity legalDong = legalDongsByCode.get(command.legalDongCode());
            List<Long> mappedRegionIds = legalDong == null
                ? List.of()
                : mappingsByLegalId.getOrDefault(legalDong.getId(), List.of())
                    .stream()
                    .filter(mapping -> isEffective(
                        mapping,
                        command.snapshotDate()
                    ))
                    .map(mapping -> mapping.getRegion().getId())
                    .toList();
            var decision = policy.resolve(
                directRegion == null ? null : directRegion.getId(),
                mappedRegionIds
            );
            if (decision.isPresent()) {
                var value = decision.get();
                RegionEntity region = regionsById.get(value.regionId());
                if (region != null) {
                    resolved.put(command.externalStoreId(), new Resolution(
                        region,
                        legalDong,
                        value.confidence(),
                        value.rule()
                    ));
                }
            } else if (legalDong != null) {
                resolved.put(command.externalStoreId(), new Resolution(
                    null,
                    legalDong,
                    null,
                    null
                ));
            }
        }
        return Map.copyOf(resolved);
    }

    private static List<String> distinctAdminCodes(
        List<SmallBusinessStoreImportCommand> commands
    ) {
        return distinctValues(commands, SmallBusinessStoreImportCommand::administrativeDongCode);
    }

    private static List<String> distinctLegalCodes(
        List<SmallBusinessStoreImportCommand> commands
    ) {
        return distinctValues(commands, SmallBusinessStoreImportCommand::legalDongCode);
    }

    private static List<String> distinctValues(
        List<SmallBusinessStoreImportCommand> commands,
        Function<SmallBusinessStoreImportCommand, String> extractor
    ) {
        return commands.stream()
            .map(extractor)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    private static boolean isEffective(
        AdministrativeLegalDongMappingEntity mapping,
        LocalDate snapshotDate
    ) {
        boolean started = mapping.getEffectiveFrom() == null
            || !snapshotDate.isBefore(mapping.getEffectiveFrom());
        boolean notEnded = mapping.getEffectiveTo() == null
            || !snapshotDate.isAfter(mapping.getEffectiveTo());
        return mapping.isUsable() && started && notEnded;
    }

    public record Resolution(
        RegionEntity region,
        LegalDongEntity legalDong,
        BigDecimal confidence,
        String rule
    ) {
    }
}
