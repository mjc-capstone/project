package com.capstone.ai_insite.dataimport.service;

import com.capstone.ai_insite.category.entity.BusinessCategoryEntity;
import com.capstone.ai_insite.category.repository.BusinessCategoryJpaRepository;
import com.capstone.ai_insite.common.exception.ResourceNotFoundException;
import com.capstone.ai_insite.metric.entity.MetricPeriodEntity;
import com.capstone.ai_insite.metric.repository.MetricPeriodJpaRepository;
import com.capstone.ai_insite.region.entity.RegionEntity;
import com.capstone.ai_insite.region.repository.RegionJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class ImportMasterResolver {

    private final RegionJpaRepository regionRepository;
    private final BusinessCategoryJpaRepository categoryRepository;
    private final MetricPeriodJpaRepository periodRepository;

    public ImportMasterResolver(
        RegionJpaRepository regionRepository,
        BusinessCategoryJpaRepository categoryRepository,
        MetricPeriodJpaRepository periodRepository
    ) {
        this.regionRepository = regionRepository;
        this.categoryRepository = categoryRepository;
        this.periodRepository = periodRepository;
    }

    public RegionEntity region(String regionCode) {
        return regionRepository.findByAdministrativeDongCodeAndActiveTrue(regionCode)
            .orElseThrow(() -> new ResourceNotFoundException("행정동을 찾을 수 없습니다: " + regionCode));
    }

    public BusinessCategoryEntity category(String categoryCode) {
        return categoryRepository.findBySourceCategoryCodeAndActiveTrue(categoryCode)
            .orElseThrow(() -> new ResourceNotFoundException("업종을 찾을 수 없습니다: " + categoryCode));
    }

    public MetricPeriodEntity period(String periodCode) {
        return periodRepository.findByPeriodCode(periodCode)
            .orElseThrow(() -> new ResourceNotFoundException("지표 기간을 찾을 수 없습니다: " + periodCode));
    }
}
