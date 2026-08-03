package com.capstone.ai_insite.metric.repository;

import com.capstone.ai_insite.metric.entity.SourceMolitCommercialTransactionEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceMolitCommercialTransactionJpaRepository
    extends JpaRepository<SourceMolitCommercialTransactionEntity, Long> {

    long deleteByDistrictCodeAndDealDateBetween(
        String districtCode,
        LocalDate startDate,
        LocalDate endDate
    );

    List<SourceMolitCommercialTransactionEntity>
        findByDealDateBetweenAndCancelledFalse(
            LocalDate startDate,
            LocalDate endDate
        );
}
