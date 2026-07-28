package com.capstone.ai_insite.region.service;

import com.capstone.ai_insite.common.exception.ResourceNotFoundException;
import com.capstone.ai_insite.region.domain.RegionMappingStatus;
import com.capstone.ai_insite.region.dto.RegionMappingResponse;
import com.capstone.ai_insite.region.dto.RegionMappingReviewRequest;
import com.capstone.ai_insite.region.repository.AdministrativeLegalDongMappingJpaRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegionMappingReviewService {

    private final AdministrativeLegalDongMappingJpaRepository repository;

    public RegionMappingReviewService(
        AdministrativeLegalDongMappingJpaRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<RegionMappingResponse> list(
        RegionMappingStatus status,
        int limit
    ) {
        var pageable = PageRequest.of(0, Math.max(1, Math.min(limit, 500)));
        var mappings = status == null
            ? repository.findAllByOrderByEvidenceCountDesc(pageable)
            : repository.findByMappingStatusOrderByEvidenceCountDesc(
                status,
                pageable
            );
        return mappings.stream().map(RegionMappingResponse::from).toList();
    }

    @Transactional
    public RegionMappingResponse confirm(
        Long mappingId,
        RegionMappingReviewRequest request
    ) {
        var mapping = find(mappingId);
        mapping.confirm(request.reviewedBy(), request.note());
        return RegionMappingResponse.from(mapping);
    }

    @Transactional
    public RegionMappingResponse reject(
        Long mappingId,
        RegionMappingReviewRequest request
    ) {
        var mapping = find(mappingId);
        mapping.reject(request.reviewedBy(), request.note());
        return RegionMappingResponse.from(mapping);
    }

    private com.capstone.ai_insite.region.entity
        .AdministrativeLegalDongMappingEntity find(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Region mapping not found: " + id
            ));
    }
}
