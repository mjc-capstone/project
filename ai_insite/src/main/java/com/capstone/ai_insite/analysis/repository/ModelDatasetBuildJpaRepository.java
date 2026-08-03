package com.capstone.ai_insite.analysis.repository;

import com.capstone.ai_insite.analysis.entity.ModelDatasetBuildEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelDatasetBuildJpaRepository
    extends JpaRepository<ModelDatasetBuildEntity, Long> {

    boolean existsByDatasetVersion(String datasetVersion);

    Optional<ModelDatasetBuildEntity> findByDatasetVersion(String datasetVersion);
}
