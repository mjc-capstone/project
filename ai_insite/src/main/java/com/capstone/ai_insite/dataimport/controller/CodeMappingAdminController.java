package com.capstone.ai_insite.dataimport.controller;

import com.capstone.ai_insite.category.domain.CategoryMappingRebuildResult;
import com.capstone.ai_insite.category.domain.MappingStatus;
import com.capstone.ai_insite.category.dto.CategoryMappingCandidateResponse;
import com.capstone.ai_insite.category.dto.CategoryMappingReviewRequest;
import com.capstone.ai_insite.category.service.CategoryMappingReviewService;
import com.capstone.ai_insite.dataimport.dto.CodeMappingSynchronizationResponse;
import com.capstone.ai_insite.dataimport.service.CodeMappingDataImportService;
import com.capstone.ai_insite.region.domain.RegionMappingRebuildResult;
import com.capstone.ai_insite.region.domain.RegionMappingStatus;
import com.capstone.ai_insite.region.dto.RegionMappingResponse;
import com.capstone.ai_insite.region.dto.RegionMappingReviewRequest;
import com.capstone.ai_insite.region.service.AdministrativeLegalDongMappingSynchronizationService;
import com.capstone.ai_insite.region.service.RegionMappingReviewService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/mappings")
public class CodeMappingAdminController {

    private final ObjectProvider<CodeMappingDataImportService> importService;
    private final CategoryMappingReviewService categoryService;
    private final AdministrativeLegalDongMappingSynchronizationService
        regionSynchronizationService;
    private final RegionMappingReviewService regionReviewService;

    public CodeMappingAdminController(
        ObjectProvider<CodeMappingDataImportService> importService,
        CategoryMappingReviewService categoryService,
        AdministrativeLegalDongMappingSynchronizationService
            regionSynchronizationService,
        RegionMappingReviewService regionReviewService
    ) {
        this.importService = importService;
        this.categoryService = categoryService;
        this.regionSynchronizationService = regionSynchronizationService;
        this.regionReviewService = regionReviewService;
    }

    @PostMapping("/synchronize")
    public CodeMappingSynchronizationResponse synchronize(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate storeSnapshotDate,
        @RequestParam(required = false) String requestedBy
    ) {
        CodeMappingDataImportService service = importService.getIfAvailable();
        if (service == null) {
            throw new IllegalStateException(
                "Public-data API integration is disabled."
            );
        }
        return service.synchronize(storeSnapshotDate, requestedBy);
    }

    @PostMapping("/categories/rebuild")
    public CategoryMappingRebuildResult rebuildCategories(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate snapshotDate
    ) {
        return categoryService.rebuild(snapshotDate);
    }

    @GetMapping("/categories")
    public List<CategoryMappingCandidateResponse> categories(
        @RequestParam(required = false) MappingStatus status,
        @RequestParam(defaultValue = "100") int limit
    ) {
        return categoryService.list(status, limit);
    }

    @PutMapping("/categories/{candidateId}/confirm")
    public CategoryMappingCandidateResponse confirmCategory(
        @PathVariable Long candidateId,
        @RequestBody CategoryMappingReviewRequest request
    ) {
        return categoryService.confirm(candidateId, request);
    }

    @PutMapping("/categories/{candidateId}/reject")
    public CategoryMappingCandidateResponse rejectCategory(
        @PathVariable Long candidateId,
        @RequestBody CategoryMappingReviewRequest request
    ) {
        return categoryService.reject(candidateId, request);
    }

    @PostMapping("/regions/rebuild")
    public RegionMappingRebuildResult rebuildRegions(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate snapshotDate
    ) {
        return regionSynchronizationService.rebuild(snapshotDate);
    }

    @GetMapping("/regions")
    public List<RegionMappingResponse> regions(
        @RequestParam(required = false) RegionMappingStatus status,
        @RequestParam(defaultValue = "100") int limit
    ) {
        return regionReviewService.list(status, limit);
    }

    @PutMapping("/regions/{mappingId}/confirm")
    public RegionMappingResponse confirmRegion(
        @PathVariable Long mappingId,
        @RequestBody RegionMappingReviewRequest request
    ) {
        return regionReviewService.confirm(mappingId, request);
    }

    @PutMapping("/regions/{mappingId}/reject")
    public RegionMappingResponse rejectRegion(
        @PathVariable Long mappingId,
        @RequestBody RegionMappingReviewRequest request
    ) {
        return regionReviewService.reject(mappingId, request);
    }
}
