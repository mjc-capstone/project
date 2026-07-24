package com.capstone.ai_insite.dataimport.controller;

import com.capstone.ai_insite.dataimport.dto.ImportResultResponse;
import com.capstone.ai_insite.dataimport.dto.SalesImportRequest;
import com.capstone.ai_insite.dataimport.dto.StoreImportRequest;
import com.capstone.ai_insite.dataimport.service.SalesImportService;
import com.capstone.ai_insite.dataimport.service.StoreImportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/import-jobs/seoul")
public class DataImportAdminController {

    private final SalesImportService salesImportService;
    private final StoreImportService storeImportService;

    public DataImportAdminController(
        SalesImportService salesImportService,
        StoreImportService storeImportService
    ) {
        this.salesImportService = salesImportService;
        this.storeImportService = storeImportService;
    }

    @PostMapping("/sales")
    public ImportResultResponse importSales(@RequestBody SalesImportRequest request) {
        return ImportResultResponse.completed(salesImportService.importSales(request.toCommand()));
    }

    @PostMapping("/stores")
    public ImportResultResponse importStores(@RequestBody StoreImportRequest request) {
        return ImportResultResponse.completed(storeImportService.importStores(request.toCommand()));
    }
}
