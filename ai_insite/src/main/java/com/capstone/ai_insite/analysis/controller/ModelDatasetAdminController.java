package com.capstone.ai_insite.analysis.controller;

import com.capstone.ai_insite.analysis.domain.DatasetSplit;
import com.capstone.ai_insite.analysis.dto.FeatureLabelBuildResponse;
import com.capstone.ai_insite.analysis.dto.ModelDatasetBuildRequest;
import com.capstone.ai_insite.analysis.dto.ModelDatasetBuildResponse;
import com.capstone.ai_insite.analysis.dto.ModelDatasetExampleResponse;
import com.capstone.ai_insite.analysis.dto.ModelEvaluationRequest;
import com.capstone.ai_insite.analysis.dto.ModelDatasetAuditResponse;
import com.capstone.ai_insite.analysis.service.ModelDatasetApplicationService;
import com.capstone.ai_insite.analysis.service.ModelFeatureLabelService;
import com.capstone.ai_insite.analysis.service.ModelDatasetNdjsonExportService;
import com.capstone.ai_insite.analysis.service.ModelDatasetAuditService;
import com.capstone.ai_insite.analysis.service.FeatureBuildService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/admin/model-datasets")
public class ModelDatasetAdminController {

    private final ModelDatasetApplicationService datasetService;
    private final ModelFeatureLabelService featureLabelService;
    private final ModelDatasetNdjsonExportService exportService;
    private final ModelDatasetAuditService auditService;
    private final ObjectMapper objectMapper;

    public ModelDatasetAdminController(
        ModelDatasetApplicationService datasetService,
        ModelFeatureLabelService featureLabelService,
        ModelDatasetNdjsonExportService exportService,
        ModelDatasetAuditService auditService,
        ObjectMapper objectMapper
    ) {
        this.datasetService = datasetService;
        this.featureLabelService = featureLabelService;
        this.exportService = exportService;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/feature-labels/rebuild")
    public FeatureLabelBuildResponse rebuild(
        @RequestParam String fromPeriod,
        @RequestParam String toPeriod
    ) {
        return FeatureLabelBuildResponse.from(
            featureLabelService.rebuild(fromPeriod, toPeriod)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ModelDatasetBuildResponse build(
        @RequestBody ModelDatasetBuildRequest request,
        @RequestParam(defaultValue = FeatureBuildService.FEATURE_VERSION)
        String featureVersion
    ) {
        return ModelDatasetBuildResponse.from(
            datasetService.build(request.toCommand(), featureVersion),
            objectMapper
        );
    }

    @GetMapping("/{datasetId}")
    public ModelDatasetBuildResponse get(@PathVariable Long datasetId) {
        return ModelDatasetBuildResponse.from(datasetService.get(datasetId), objectMapper);
    }

    @PutMapping("/{datasetId}/evaluation")
    public ModelDatasetBuildResponse recordEvaluation(
        @PathVariable Long datasetId,
        @RequestBody ModelEvaluationRequest request
    ) {
        return ModelDatasetBuildResponse.from(
            datasetService.recordEvaluation(datasetId, request.toCommand()),
            objectMapper
        );
    }

    @GetMapping("/{datasetId}/examples")
    public List<ModelDatasetExampleResponse> examples(
        @PathVariable Long datasetId,
        @RequestParam(required = false) DatasetSplit split
    ) {
        return datasetService.getExamples(datasetId, split)
            .stream()
            .map(example -> ModelDatasetExampleResponse.from(example, objectMapper))
            .toList();
    }

    @GetMapping(value = "/{datasetId}/export", produces = "application/x-ndjson")
    public ResponseEntity<StreamingResponseBody> export(
        @PathVariable Long datasetId,
        @RequestParam(required = false) DatasetSplit split
    ) {
        StreamingResponseBody body = output -> exportService.export(
            datasetId,
            split,
            output
        );
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/x-ndjson"))
            .header(
                "Content-Disposition",
                "attachment; filename=dataset-" + datasetId + ".ndjson"
            )
            .body(body);
    }

    @GetMapping("/{datasetId}/audit")
    public ModelDatasetAuditResponse audit(@PathVariable Long datasetId) {
        return ModelDatasetAuditResponse.from(auditService.audit(datasetId));
    }
}
