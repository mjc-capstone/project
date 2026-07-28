package com.capstone.ai_insite.dataimport.dto;

import com.capstone.ai_insite.dataimport.domain.DataImportJobStatus;
import com.capstone.ai_insite.dataimport.entity.DataImportJobEntity;
import java.time.LocalDateTime;

public record DataImportJobResponse(
    Long id,
    String sourceName,
    String serviceName,
    String targetPeriod,
    DataImportJobStatus status,
    LocalDateTime startedAt,
    LocalDateTime completedAt,
    String requestedBy,
    int totalPageCount,
    long fetchedRowCount,
    long normalizedRowCount,
    long rejectedRowCount,
    String errorMessage,
    Long retryOfJobId
) {
    public static DataImportJobResponse from(DataImportJobEntity entity) {
        return new DataImportJobResponse(
            entity.getId(),
            entity.getSourceName(),
            entity.getServiceName(),
            entity.getTargetPeriod(),
            entity.getStatus(),
            entity.getStartedAt(),
            entity.getCompletedAt(),
            entity.getRequestedBy(),
            entity.getTotalPageCount(),
            entity.getFetchedRowCount(),
            entity.getNormalizedRowCount(),
            entity.getRejectedRowCount(),
            entity.getErrorMessage(),
            entity.getRetryOfJob() == null ? null : entity.getRetryOfJob().getId()
        );
    }
}
