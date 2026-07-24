package com.capstone.ai_insite.region.service;

import com.capstone.ai_insite.common.exception.ResourceNotFoundException;
import com.capstone.ai_insite.region.domain.LegalDong;
import com.capstone.ai_insite.region.domain.Region;
import com.capstone.ai_insite.region.entity.LegalDongEntity;
import com.capstone.ai_insite.region.entity.RegionEntity;
import com.capstone.ai_insite.region.repository.AdministrativeLegalDongMappingJpaRepository;
import com.capstone.ai_insite.region.repository.RegionJpaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RegionQueryService {

    private final RegionJpaRepository regionRepository;
    private final AdministrativeLegalDongMappingJpaRepository mappingRepository;

    public RegionQueryService(
        RegionJpaRepository regionRepository,
        AdministrativeLegalDongMappingJpaRepository mappingRepository
    ) {
        this.regionRepository = regionRepository;
        this.mappingRepository = mappingRepository;
    }

    public List<Region> search(String keyword) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return regionRepository.searchActive(normalizedKeyword).stream()
            .map(RegionQueryService::toDomain)
            .toList();
    }

    public Region getByCode(String regionCode) {
        return toDomain(findEntity(regionCode));
    }

    public List<LegalDong> getLegalDongs(String regionCode) {
        findEntity(regionCode);
        return mappingRepository
            .findByRegionAdministrativeDongCodeOrderByMappingConfidenceDesc(regionCode)
            .stream()
            .map(mapping -> toDomain(mapping.getLegalDong()))
            .toList();
    }

    private RegionEntity findEntity(String regionCode) {
        return regionRepository.findByAdministrativeDongCodeAndActiveTrue(regionCode)
            .orElseThrow(() -> new ResourceNotFoundException("행정동을 찾을 수 없습니다: " + regionCode));
    }

    private static Region toDomain(RegionEntity entity) {
        return new Region(
            entity.getId(),
            entity.getAdministrativeDongCode(),
            entity.getSidoName(),
            entity.getSigunguName(),
            entity.getAdministrativeDongName(),
            entity.getLatitude(),
            entity.getLongitude()
        );
    }

    private static LegalDong toDomain(LegalDongEntity entity) {
        return new LegalDong(
            entity.getId(),
            entity.getLegalDongCode(),
            entity.getSidoName(),
            entity.getSigunguName(),
            entity.getLegalDongName(),
            entity.getEffectiveFrom(),
            entity.getEffectiveTo()
        );
    }
}
