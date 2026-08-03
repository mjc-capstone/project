package com.capstone.ai_insite.analysis.repository;

import com.capstone.ai_insite.analysis.domain.DatasetSplit;
import com.capstone.ai_insite.analysis.entity.ModelDatasetMemberEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelDatasetMemberJpaRepository
    extends JpaRepository<ModelDatasetMemberEntity, Long> {

    List<ModelDatasetMemberEntity>
        findByDatasetBuildIdOrderByFeatureSnapshotMetricPeriodStartDateAsc(Long datasetId);

    List<ModelDatasetMemberEntity>
        findByDatasetBuildIdAndDatasetSplitOrderByFeatureSnapshotMetricPeriodStartDateAsc(
            Long datasetId,
            DatasetSplit split
        );

    Slice<ModelDatasetMemberEntity> findByDatasetBuildIdOrderByIdAsc(
        Long datasetId,
        Pageable pageable
    );

    Slice<ModelDatasetMemberEntity> findByDatasetBuildIdAndDatasetSplitOrderByIdAsc(
        Long datasetId,
        DatasetSplit split,
        Pageable pageable
    );
}
