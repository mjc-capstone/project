package com.capstone.ai_insite.region.controller;

import com.capstone.ai_insite.region.dto.LegalDongResponse;
import com.capstone.ai_insite.region.dto.RegionResponse;
import com.capstone.ai_insite.region.service.RegionQueryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/regions")
public class RegionController {

    private final RegionQueryService regionQueryService;

    public RegionController(RegionQueryService regionQueryService) {
        this.regionQueryService = regionQueryService;
    }

    @GetMapping
    public List<RegionResponse> search(
        @RequestParam(required = false) String keyword
    ) {
        return regionQueryService.search(keyword).stream().map(RegionResponse::from).toList();
    }

    @GetMapping("/{regionCode}")
    public RegionResponse get(@PathVariable String regionCode) {
        return RegionResponse.from(regionQueryService.getByCode(regionCode));
    }

    @GetMapping("/{regionCode}/legal-dongs")
    public List<LegalDongResponse> getLegalDongs(@PathVariable String regionCode) {
        return regionQueryService.getLegalDongs(regionCode).stream()
            .map(LegalDongResponse::from)
            .toList();
    }
}
