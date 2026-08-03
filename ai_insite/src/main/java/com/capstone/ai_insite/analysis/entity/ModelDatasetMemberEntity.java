package com.capstone.ai_insite.analysis.entity;

import com.capstone.ai_insite.analysis.domain.DatasetSplit;
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
@Table(name = "model_dataset_members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ModelDatasetMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_dataset_build_id", nullable = false)
    private ModelDatasetBuildEntity datasetBuild;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_feature_snapshot_id", nullable = false)
    private ModelFeatureSnapshotEntity featureSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "dataset_split", nullable = false, length = 20)
    private DatasetSplit datasetSplit;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public ModelDatasetMemberEntity(
        ModelDatasetBuildEntity datasetBuild,
        ModelFeatureSnapshotEntity featureSnapshot,
        DatasetSplit datasetSplit
    ) {
        this.datasetBuild = datasetBuild;
        this.featureSnapshot = featureSnapshot;
        this.datasetSplit = datasetSplit;
    }
}
