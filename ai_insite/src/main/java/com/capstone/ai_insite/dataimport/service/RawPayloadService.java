package com.capstone.ai_insite.dataimport.service;

import com.capstone.ai_insite.dataimport.domain.RawPayloadCommand;
import com.capstone.ai_insite.dataimport.entity.RawApiPayloadEntity;
import com.capstone.ai_insite.dataimport.repository.RawApiPayloadJpaRepository;
import com.capstone.ai_insite.dataimport.repository.DataImportJobJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RawPayloadService {

    private final RawApiPayloadJpaRepository rawPayloadRepository;
    private final DataImportJobJpaRepository jobRepository;

    public RawPayloadService(
        RawApiPayloadJpaRepository rawPayloadRepository,
        DataImportJobJpaRepository jobRepository
    ) {
        this.rawPayloadRepository = rawPayloadRepository;
        this.jobRepository = jobRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RawApiPayloadEntity save(RawPayloadCommand command) {
        var job = command.dataImportJobId() == null
            ? null
            : jobRepository.findById(command.dataImportJobId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "원본 응답을 연결할 수집 작업을 찾을 수 없습니다: "
                        + command.dataImportJobId()
                ));
        return rawPayloadRepository.save(new RawApiPayloadEntity(
            command.sourceName(),
            command.serviceName(),
            command.requestUrl(),
            command.requestParamsJson(),
            command.responseBodyJson(),
            command.rowCount(),
            "FETCHED",
            job
        ));
    }
}
