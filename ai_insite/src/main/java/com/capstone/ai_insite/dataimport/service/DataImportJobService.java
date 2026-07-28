package com.capstone.ai_insite.dataimport.service;

import com.capstone.ai_insite.common.exception.ActiveDataImportJobException;
import com.capstone.ai_insite.common.exception.ResourceNotFoundException;
import com.capstone.ai_insite.dataimport.domain.DataImportJobProgress;
import com.capstone.ai_insite.dataimport.domain.DataImportJobStatus;
import com.capstone.ai_insite.dataimport.entity.DataImportJobEntity;
import com.capstone.ai_insite.dataimport.repository.DataImportJobJpaRepository;
import java.util.EnumSet;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataImportJobService {

    private static final EnumSet<DataImportJobStatus> ACTIVE_STATUSES =
        EnumSet.of(DataImportJobStatus.PENDING, DataImportJobStatus.RUNNING);

    private final DataImportJobJpaRepository repository;

    public DataImportJobService(DataImportJobJpaRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DataImportJobEntity start(
        String sourceName,
        String serviceName,
        String targetPeriod,
        String requestedBy,
        Long retryOfJobId
    ) {
        if (repository.existsBySourceNameAndServiceNameAndTargetPeriodAndStatusIn(
            sourceName,
            serviceName,
            targetPeriod,
            ACTIVE_STATUSES
        )) {
            throw activeJob(sourceName, serviceName, targetPeriod);
        }
        DataImportJobEntity retryOf = retryOfJobId == null
            ? null
            : repository.findById(retryOfJobId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "재시도할 수집 작업을 찾을 수 없습니다: " + retryOfJobId
                ));
        if (retryOf != null && retryOf.getStatus() != DataImportJobStatus.FAILED) {
            throw new IllegalArgumentException("실패한 수집 작업만 재시도할 수 있습니다.");
        }
        if (retryOf != null
            && (!retryOf.getSourceName().equals(sourceName)
                || !retryOf.getServiceName().equals(serviceName)
                || !retryOf.getTargetPeriod().equals(targetPeriod))) {
            throw new IllegalArgumentException(
                "A retry must use the same source, service, and target period."
            );
        }
        DataImportJobEntity job = DataImportJobEntity.pending(
            sourceName,
            serviceName,
            targetPeriod,
            requestedBy,
            retryOf
        );
        job.start();
        try {
            return repository.saveAndFlush(job);
        } catch (DataIntegrityViolationException exception) {
            throw activeJob(sourceName, serviceName, targetPeriod);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long jobId, DataImportJobProgress progress) {
        find(jobId).record(progress);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(Long jobId, DataImportJobProgress progress) {
        find(jobId).complete(progress);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(Long jobId, Throwable throwable) {
        find(jobId).fail(rootMessage(throwable));
    }

    @Transactional(readOnly = true)
    public DataImportJobEntity get(Long jobId) {
        return find(jobId);
    }

    private DataImportJobEntity find(Long jobId) {
        return repository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "수집 작업을 찾을 수 없습니다: " + jobId
            ));
    }

    private static ActiveDataImportJobException activeJob(
        String sourceName,
        String serviceName,
        String targetPeriod
    ) {
        return new ActiveDataImportJobException(
            "동일한 수집 작업이 이미 실행 중입니다: "
                + sourceName + "/" + serviceName + "/" + targetPeriod
        );
    }

    private static String rootMessage(Throwable throwable) {
        StringBuilder message = new StringBuilder();
        Throwable current = throwable;
        while (current != null) {
            String currentMessage = current.getMessage();
            if (currentMessage != null
                && !currentMessage.isBlank()
                && !message.toString().contains(currentMessage)) {
                if (!message.isEmpty()) {
                    message.append(" | caused by: ");
                }
                message.append(currentMessage);
            }
            current = current.getCause();
        }
        return message.isEmpty()
            ? throwable.getClass().getSimpleName()
            : message.toString();
    }
}
