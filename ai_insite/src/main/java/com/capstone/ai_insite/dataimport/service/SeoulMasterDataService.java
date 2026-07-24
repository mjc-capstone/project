package com.capstone.ai_insite.dataimport.service;

import com.capstone.ai_insite.category.entity.BusinessCategoryEntity;
import com.capstone.ai_insite.category.repository.BusinessCategoryJpaRepository;
import com.capstone.ai_insite.dataimport.domain.SeoulMasterReference;
import com.capstone.ai_insite.dataimport.domain.SeoulQuarter;
import com.capstone.ai_insite.dataimport.domain.SeoulRegionPeriodReference;
import com.capstone.ai_insite.metric.entity.MetricPeriodEntity;
import com.capstone.ai_insite.metric.repository.MetricPeriodJpaRepository;
import com.capstone.ai_insite.region.domain.SeoulDistrict;
import com.capstone.ai_insite.region.entity.RegionEntity;
import com.capstone.ai_insite.region.repository.RegionJpaRepository;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeoulMasterDataService {

    private final RegionJpaRepository regionRepository;
    private final BusinessCategoryJpaRepository categoryRepository;
    private final MetricPeriodJpaRepository periodRepository;

    public SeoulMasterDataService(
        RegionJpaRepository regionRepository,
        BusinessCategoryJpaRepository categoryRepository,
        MetricPeriodJpaRepository periodRepository
    ) {
        this.regionRepository = regionRepository;
        this.categoryRepository = categoryRepository;
        this.periodRepository = periodRepository;
    }

    @Transactional
    public SeoulMasterSnapshot synchronize(Collection<SeoulMasterReference> references) {
        if (references.isEmpty()) {
            return new SeoulMasterSnapshot(Map.of(), Map.of(), Map.of());
        }
        return new SeoulMasterSnapshot(
            synchronizeRegions(references),
            synchronizeCategories(references),
            synchronizePeriods(references)
        );
    }

    @Transactional
    public SeoulMasterSnapshot synchronizeRegionPeriods(
        Collection<? extends SeoulRegionPeriodReference> references
    ) {
        if (references.isEmpty()) {
            return new SeoulMasterSnapshot(Map.of(), Map.of(), Map.of());
        }
        return new SeoulMasterSnapshot(
            synchronizeRegions(references),
            Map.of(),
            synchronizePeriods(references)
        );
    }

    private Map<String, RegionEntity> synchronizeRegions(
        Collection<? extends SeoulRegionPeriodReference> references
    ) {
        Map<String, SeoulRegionPeriodReference> distinct = references.stream()
            .collect(Collectors.toMap(
                SeoulRegionPeriodReference::regionCode,
                Function.identity(),
                (first, ignored) -> first,
                LinkedHashMap::new
            ));
        Map<String, RegionEntity> entities = regionRepository
            .findByAdministrativeDongCodeIn(distinct.keySet())
            .stream()
            .collect(Collectors.toMap(
                RegionEntity::getAdministrativeDongCode,
                Function.identity()
            ));
        distinct.forEach((code, reference) -> {
            SeoulDistrict district = SeoulDistrict.fromAdministrativeDongCode(code);
            RegionEntity entity = entities.computeIfAbsent(
                code,
                ignored -> RegionEntity.createSeoulAdministrativeDong(
                    code,
                    reference.regionName(),
                    district.code(),
                    district.districtName()
                )
            );
            entity.synchronizeSeoulAdministrativeDong(
                code,
                reference.regionName(),
                district.code(),
                district.districtName()
            );
        });
        List<RegionEntity> saved = regionRepository.saveAll(entities.values());
        return saved.stream().collect(Collectors.toMap(
            RegionEntity::getAdministrativeDongCode,
            Function.identity()
        ));
    }

    private Map<String, BusinessCategoryEntity> synchronizeCategories(
        Collection<SeoulMasterReference> references
    ) {
        Map<String, SeoulMasterReference> distinct = references.stream()
            .collect(Collectors.toMap(
                SeoulMasterReference::categoryCode,
                Function.identity(),
                (first, ignored) -> first,
                LinkedHashMap::new
            ));
        Map<String, BusinessCategoryEntity> entities = categoryRepository
            .findBySourceSystemAndSourceCategoryCodeIn(
                BusinessCategoryEntity.SEOUL_COMMERCIAL_SOURCE,
                distinct.keySet()
            )
            .stream()
            .collect(Collectors.toMap(
                BusinessCategoryEntity::getSourceCategoryCode,
                Function.identity()
            ));
        distinct.forEach((code, reference) -> {
            BusinessCategoryEntity entity = entities.computeIfAbsent(
                code,
                ignored -> BusinessCategoryEntity.createSeoulCommercial(
                    code,
                    reference.categoryName()
                )
            );
            entity.synchronizeSeoulCommercial(code, reference.categoryName());
        });
        List<BusinessCategoryEntity> saved = categoryRepository.saveAll(entities.values());
        return saved.stream().collect(Collectors.toMap(
            BusinessCategoryEntity::getSourceCategoryCode,
            Function.identity()
        ));
    }

    private Map<String, MetricPeriodEntity> synchronizePeriods(
        Collection<? extends SeoulRegionPeriodReference> references
    ) {
        Map<String, SeoulQuarter> quarters = references.stream()
            .map(reference -> SeoulQuarter.parse(reference.sourcePeriodCode()))
            .collect(Collectors.toMap(
                SeoulQuarter::periodCode,
                Function.identity(),
                (first, ignored) -> first,
                LinkedHashMap::new
            ));
        Map<String, MetricPeriodEntity> entities = periodRepository
            .findByPeriodCodeIn(quarters.keySet())
            .stream()
            .collect(Collectors.toMap(MetricPeriodEntity::getPeriodCode, Function.identity()));
        quarters.forEach((periodCode, quarter) -> entities.computeIfAbsent(
            periodCode,
            ignored -> MetricPeriodEntity.createQuarter(
                periodCode,
                quarter.year(),
                quarter.quarter()
            )
        ));
        List<MetricPeriodEntity> saved = periodRepository.saveAll(entities.values());
        return saved.stream().collect(Collectors.toMap(
            MetricPeriodEntity::getPeriodCode,
            Function.identity()
        ));
    }
}
