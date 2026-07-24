package com.capstone.ai_insite.dataimport.service;

import com.capstone.ai_insite.dataimport.domain.RawPayloadCommand;
import com.capstone.ai_insite.dataimport.entity.RawApiPayloadEntity;
import com.capstone.ai_insite.dataimport.repository.RawApiPayloadJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RawPayloadService {

    private final RawApiPayloadJpaRepository rawPayloadRepository;

    public RawPayloadService(RawApiPayloadJpaRepository rawPayloadRepository) {
        this.rawPayloadRepository = rawPayloadRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RawApiPayloadEntity save(RawPayloadCommand command) {
        return rawPayloadRepository.save(new RawApiPayloadEntity(
            command.sourceName(),
            command.serviceName(),
            command.requestUrl(),
            command.requestParamsJson(),
            command.responseBodyJson(),
            command.rowCount(),
            "FETCHED"
        ));
    }
}
