package com.capstone.ai_insite.dataimport.repository;

import com.capstone.ai_insite.dataimport.domain.DataImportJobStatus;
import com.capstone.ai_insite.dataimport.entity.DataImportJobEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataImportJobJpaRepository
    extends JpaRepository<DataImportJobEntity, Long> {

    boolean existsBySourceNameAndServiceNameAndTargetPeriodAndStatusIn(
        String sourceName,
        String serviceName,
        String targetPeriod,
        Collection<DataImportJobStatus> statuses
    );

    List<DataImportJobEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<DataImportJobEntity> findByStatusOrderByCreatedAtDesc(
        DataImportJobStatus status,
        Pageable pageable
    );
}
