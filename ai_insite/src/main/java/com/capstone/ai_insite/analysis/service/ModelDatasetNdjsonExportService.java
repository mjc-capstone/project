package com.capstone.ai_insite.analysis.service;

import com.capstone.ai_insite.analysis.domain.DatasetSplit;
import com.capstone.ai_insite.analysis.entity.ModelDatasetMemberEntity;
import com.capstone.ai_insite.analysis.entity.ModelFeatureSnapshotEntity;
import com.capstone.ai_insite.analysis.repository.ModelDatasetBuildJpaRepository;
import com.capstone.ai_insite.analysis.repository.ModelDatasetMemberJpaRepository;
import com.capstone.ai_insite.common.exception.ResourceNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Service
public class ModelDatasetNdjsonExportService {

    private static final int PAGE_SIZE = 500;

    private final ModelDatasetBuildJpaRepository datasetRepository;
    private final ModelDatasetMemberJpaRepository memberRepository;
    private final ObjectMapper objectMapper;

    public ModelDatasetNdjsonExportService(
        ModelDatasetBuildJpaRepository datasetRepository,
        ModelDatasetMemberJpaRepository memberRepository,
        ObjectMapper objectMapper
    ) {
        this.datasetRepository = datasetRepository;
        this.memberRepository = memberRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public void export(Long datasetId, DatasetSplit split, OutputStream output) {
        if (!datasetRepository.existsById(datasetId)) {
            throw new ResourceNotFoundException(
                "모델 데이터셋을 찾을 수 없습니다: " + datasetId
            );
        }
        long lastId = 0L;
        Slice<ModelDatasetMemberEntity> members;
        do {
            PageRequest pageable = PageRequest.of(0, PAGE_SIZE);
            members = split == null
                ? memberRepository.findByDatasetBuildIdAndIdGreaterThanOrderByIdAsc(
                    datasetId,
                    lastId,
                    pageable
                )
                : memberRepository
                    .findByDatasetBuildIdAndDatasetSplitAndIdGreaterThanOrderByIdAsc(
                    datasetId,
                    split,
                    lastId,
                    pageable
                );
            for (ModelDatasetMemberEntity member : members) {
                writeRow(member, output);
            }
            if (members.hasContent()) {
                lastId = members.getContent().getLast().getId();
            }
        } while (members.hasNext());
    }

    private void writeRow(
        ModelDatasetMemberEntity member,
        OutputStream output
    ) {
        ModelFeatureSnapshotEntity feature = member.getFeatureSnapshot();
        try {
            ObjectNode row = objectMapper.createObjectNode();
            row.put("featureSnapshotId", feature.getId());
            row.put("regionCode", feature.getRegion().getAdministrativeDongCode());
            row.put("categoryCode", feature.getBusinessCategory().getSourceCategoryCode());
            row.put("featurePeriod", feature.getMetricPeriod().getPeriodCode());
            row.put("featureAsOfDate", feature.getFeatureAsOfDate().toString());
            row.put("labelPeriod", feature.getLabelPeriod().getPeriodCode());
            row.put(
                "labelHorizonPeriod",
                feature.getLabelHorizonPeriod().getPeriodCode()
            );
            row.put("split", member.getDatasetSplit().name());
            row.put("featureVersion", feature.getFeatureVersion());
            row.put("labelVersion", feature.getLabelVersion());
            row.set("features", objectMapper.readTree(feature.getFeatureJson()));
            row.set("labels", objectMapper.readTree(feature.getLabelJson()));
            output.write(objectMapper.writeValueAsBytes(row));
            output.write('\n');
        } catch (IOException exception) {
            throw new UncheckedIOException("모델 데이터셋 NDJSON 출력에 실패했습니다.", exception);
        }
    }
}
