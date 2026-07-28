package com.capstone.ai_insite.region.service;

import com.capstone.ai_insite.dataimport.repository.SourceSmallBusinessStoreJpaRepository;
import com.capstone.ai_insite.region.domain.RegionMappingRebuildResult;
import com.capstone.ai_insite.region.domain.RegionMappingStatus;
import com.capstone.ai_insite.region.entity.AdministrativeLegalDongMappingEntity;
import com.capstone.ai_insite.region.entity.LegalDongEntity;
import com.capstone.ai_insite.region.entity.RegionEntity;
import com.capstone.ai_insite.region.repository.AdministrativeLegalDongMappingJpaRepository;
import com.capstone.ai_insite.region.repository.LegalDongJpaRepository;
import com.capstone.ai_insite.region.repository.RegionJpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdministrativeLegalDongMappingSynchronizationService {

    private final SourceSmallBusinessStoreJpaRepository storeRepository;
    private final RegionJpaRepository regionRepository;
    private final LegalDongJpaRepository legalDongRepository;
    private final AdministrativeLegalDongMappingJpaRepository mappingRepository;

    public AdministrativeLegalDongMappingSynchronizationService(
        SourceSmallBusinessStoreJpaRepository storeRepository,
        RegionJpaRepository regionRepository,
        LegalDongJpaRepository legalDongRepository,
        AdministrativeLegalDongMappingJpaRepository mappingRepository
    ) {
        this.storeRepository = storeRepository;
        this.regionRepository = regionRepository;
        this.legalDongRepository = legalDongRepository;
        this.mappingRepository = mappingRepository;
    }

    @Transactional
    public RegionMappingRebuildResult rebuild(LocalDate requestedSnapshotDate) {
        LocalDate snapshotDate = requestedSnapshotDate == null
            ? storeRepository.findLatestSnapshotDate()
            : requestedSnapshotDate;
        if (snapshotDate == null) {
            throw new IllegalStateException("No store snapshot is available.");
        }
        int linkedStoreCount = storeRepository.linkLegalDongs(snapshotDate);
        var pairs = storeRepository.aggregateRegionCodePairs(snapshotDate);
        Set<String> regionCodes = pairs.stream()
            .map(SourceSmallBusinessStoreJpaRepository
                .RegionCodePairEvidence::getAdministrativeDongCode)
            .collect(Collectors.toSet());
        Set<String> legalCodes = pairs.stream()
            .map(SourceSmallBusinessStoreJpaRepository
                .RegionCodePairEvidence::getLegalDongCode)
            .collect(Collectors.toSet());
        Map<String, RegionEntity> regions = regionRepository
            .findByAdministrativeDongCodeIn(regionCodes)
            .stream()
            .collect(Collectors.toMap(
                RegionEntity::getAdministrativeDongCode,
                Function.identity()
            ));
        Map<String, LegalDongEntity> legalDongs = legalDongRepository
            .findByLegalDongCodeIn(legalCodes)
            .stream()
            .collect(Collectors.toMap(
                LegalDongEntity::getLegalDongCode,
                Function.identity()
            ));

        int autoConfirmed = 0;
        int candidates = 0;
        int unresolved = 0;
        for (var pair : pairs) {
            RegionEntity region = regions.get(pair.getAdministrativeDongCode());
            LegalDongEntity legalDong = legalDongs.get(pair.getLegalDongCode());
            if (region == null || legalDong == null) {
                unresolved++;
                continue;
            }
            AdministrativeLegalDongMappingEntity mapping = mappingRepository
                .findFirstByRegionIdAndLegalDongIdOrderByIdDesc(
                    region.getId(),
                    legalDong.getId()
                )
                .orElseGet(() ->
                    AdministrativeLegalDongMappingEntity.create(region, legalDong)
                );
            mapping.synchronizeObserved(pair.getEvidenceCount(), snapshotDate);
            mappingRepository.save(mapping);
            if (mapping.getMappingStatus() == RegionMappingStatus.AUTO_CONFIRMED
                || mapping.getMappingStatus() == RegionMappingStatus.CONFIRMED) {
                autoConfirmed++;
            } else {
                candidates++;
            }
        }
        return new RegionMappingRebuildResult(
            snapshotDate,
            linkedStoreCount,
            pairs.size(),
            autoConfirmed,
            candidates,
            unresolved
        );
    }
}
