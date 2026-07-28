package com.capstone.ai_insite.dataimport.entity;

import com.capstone.ai_insite.dataimport.domain.DataImportJobProgress;
import com.capstone.ai_insite.dataimport.domain.DataImportJobStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "data_import_jobs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DataImportJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_name", nullable = false, length = 100)
    private String sourceName;

    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @Column(name = "target_period", nullable = false, length = 20)
    private String targetPeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DataImportJobStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "requested_by", length = 100)
    private String requestedBy;

    @Column(name = "total_page_count", nullable = false)
    private int totalPageCount;

    @Column(name = "fetched_row_count", nullable = false)
    private long fetchedRowCount;

    @Column(name = "normalized_row_count", nullable = false)
    private long normalizedRowCount;

    @Column(name = "rejected_row_count", nullable = false)
    private long rejectedRowCount;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retry_of_job_id")
    private DataImportJobEntity retryOfJob;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public static DataImportJobEntity pending(
        String sourceName,
        String serviceName,
        String targetPeriod,
        String requestedBy,
        DataImportJobEntity retryOfJob
    ) {
        DataImportJobEntity entity = new DataImportJobEntity();
        entity.sourceName = required(sourceName, "sourceName");
        entity.serviceName = required(serviceName, "serviceName");
        entity.targetPeriod = required(targetPeriod, "targetPeriod");
        entity.requestedBy = blankToNull(requestedBy);
        entity.retryOfJob = retryOfJob;
        entity.status = DataImportJobStatus.PENDING;
        return entity;
    }

    public void start() {
        requireStatus(DataImportJobStatus.PENDING);
        status = DataImportJobStatus.RUNNING;
        startedAt = LocalDateTime.now();
    }

    public void record(DataImportJobProgress progress) {
        requireStatus(DataImportJobStatus.RUNNING);
        totalPageCount = progress.pageCount();
        fetchedRowCount = progress.fetchedRowCount();
        normalizedRowCount = progress.normalizedRowCount();
        rejectedRowCount = progress.rejectedRowCount();
    }

    public void complete(DataImportJobProgress progress) {
        record(progress);
        status = DataImportJobStatus.COMPLETED;
        completedAt = LocalDateTime.now();
        errorMessage = null;
    }

    public void fail(String message) {
        if (status != DataImportJobStatus.PENDING
            && status != DataImportJobStatus.RUNNING) {
            throw new IllegalStateException("실행 중인 수집 작업만 실패 처리할 수 있습니다.");
        }
        status = DataImportJobStatus.FAILED;
        completedAt = LocalDateTime.now();
        errorMessage = blankToNull(message);
    }

    private void requireStatus(DataImportJobStatus expected) {
        if (status != expected) {
            throw new IllegalStateException(
                "수집 작업 상태가 올바르지 않습니다: " + status
            );
        }
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + "은 필수입니다.");
        }
        return value.trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
