package com.capstone.ai_insite.analysis.repository;

import com.capstone.ai_insite.analysis.entity.AnalysisRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRequestJpaRepository extends JpaRepository<AnalysisRequestEntity, Long> {
}
