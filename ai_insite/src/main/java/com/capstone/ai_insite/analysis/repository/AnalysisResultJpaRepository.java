package com.capstone.ai_insite.analysis.repository;

import com.capstone.ai_insite.analysis.entity.AnalysisResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisResultJpaRepository extends JpaRepository<AnalysisResultEntity, Long> {
}
