package com.capstone.ai_insite.dataimport.service;

import com.capstone.ai_insite.dataimport.domain.DataImportJobStatus;
import com.capstone.ai_insite.dataimport.dto.DataImportJobResponse;
import com.capstone.ai_insite.dataimport.repository.DataImportJobJpaRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataImportJobQueryService {

    private final DataImportJobJpaRepository repository;
    private final DataImportJobService jobService;

    public DataImportJobQueryService(
        DataImportJobJpaRepository repository,
        DataImportJobService jobService
    ) {
        this.repository = repository;
        this.jobService = jobService;
    }

    @Transactional(readOnly = true)
    public List<DataImportJobResponse> list(
        DataImportJobStatus status,
        int limit
    ) {
        if (limit < 1 || limit > 200) {
            throw new IllegalArgumentException("limit은 1~200 범위여야 합니다.");
        }
        var pageable = PageRequest.of(0, limit);
        var jobs = status == null
            ? repository.findAllByOrderByCreatedAtDesc(pageable)
            : repository.findByStatusOrderByCreatedAtDesc(status, pageable);
        return jobs.stream().map(DataImportJobResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public DataImportJobResponse get(Long jobId) {
        return DataImportJobResponse.from(jobService.get(jobId));
    }
}
