package com.capstone.ai_insite.dataimport.service;

import com.capstone.ai_insite.dataimport.domain.UnmappedCodeType;
import com.capstone.ai_insite.dataimport.dto.UnmappedCodeResponse;
import com.capstone.ai_insite.dataimport.repository.SourceSmallBusinessStoreJpaRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UnmappedStoreCodeQueryService {

    private final SourceSmallBusinessStoreJpaRepository repository;

    public UnmappedStoreCodeQueryService(
        SourceSmallBusinessStoreJpaRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<UnmappedCodeResponse> find(
        UnmappedCodeType type,
        LocalDate snapshotDate
    ) {
        LocalDate target = snapshotDate == null
            ? repository.findLatestSnapshotDate()
            : snapshotDate;
        if (target == null) {
            return List.of();
        }
        return switch (type) {
            case CATEGORY -> repository.aggregateUnmappedCategories(target).stream()
                .map(row -> new UnmappedCodeResponse(
                    type,
                    row.getSourceCode(),
                    row.getSourceName(),
                    row.getRowCount(),
                    target
                ))
                .toList();
            case REGION -> repository.aggregateUnmappedRegions(target).stream()
                .map(row -> new UnmappedCodeResponse(
                    type,
                    row.getSourceCode(),
                    null,
                    row.getRowCount(),
                    target
                ))
                .toList();
        };
    }
}
