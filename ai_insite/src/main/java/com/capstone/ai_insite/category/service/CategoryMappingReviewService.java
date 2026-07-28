package com.capstone.ai_insite.category.service;

import com.capstone.ai_insite.category.domain.CategoryMappingRebuildResult;
import com.capstone.ai_insite.category.domain.MappingReviewType;
import com.capstone.ai_insite.category.domain.MappingStatus;
import com.capstone.ai_insite.category.domain.policy.CategoryMappingCandidatePolicy;
import com.capstone.ai_insite.category.dto.CategoryMappingCandidateResponse;
import com.capstone.ai_insite.category.dto.CategoryMappingReviewRequest;
import com.capstone.ai_insite.category.entity.BusinessCategoryEntity;
import com.capstone.ai_insite.category.entity.CategoryCodeMappingEntity;
import com.capstone.ai_insite.category.entity.CategoryMappingCandidateEntity;
import com.capstone.ai_insite.category.entity.SmallBusinessCategoryEntity;
import com.capstone.ai_insite.category.repository.BusinessCategoryJpaRepository;
import com.capstone.ai_insite.category.repository.CategoryCodeMappingJpaRepository;
import com.capstone.ai_insite.category.repository.CategoryMappingCandidateJpaRepository;
import com.capstone.ai_insite.category.repository.SmallBusinessCategoryJpaRepository;
import com.capstone.ai_insite.common.exception.ResourceNotFoundException;
import com.capstone.ai_insite.dataimport.repository.SourceSmallBusinessStoreJpaRepository;
import com.capstone.ai_insite.metric.repository.CommercialCompetitionFeatureJpaRepository;
import com.capstone.ai_insite.metric.service.CompetitionFeatureAggregationService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class CategoryMappingReviewService {

    private final SmallBusinessCategoryJpaRepository sourceCategoryRepository;
    private final BusinessCategoryJpaRepository targetCategoryRepository;
    private final CategoryMappingCandidateJpaRepository candidateRepository;
    private final CategoryCodeMappingJpaRepository mappingRepository;
    private final SourceSmallBusinessStoreJpaRepository storeRepository;
    private final CommercialCompetitionFeatureJpaRepository competitionRepository;
    private final CompetitionFeatureAggregationService aggregationService;
    private final CategoryMappingCandidatePolicy policy;
    private final ObjectMapper objectMapper;

    public CategoryMappingReviewService(
        SmallBusinessCategoryJpaRepository sourceCategoryRepository,
        BusinessCategoryJpaRepository targetCategoryRepository,
        CategoryMappingCandidateJpaRepository candidateRepository,
        CategoryCodeMappingJpaRepository mappingRepository,
        SourceSmallBusinessStoreJpaRepository storeRepository,
        CommercialCompetitionFeatureJpaRepository competitionRepository,
        CompetitionFeatureAggregationService aggregationService,
        CategoryMappingCandidatePolicy policy,
        ObjectMapper objectMapper
    ) {
        this.sourceCategoryRepository = sourceCategoryRepository;
        this.targetCategoryRepository = targetCategoryRepository;
        this.candidateRepository = candidateRepository;
        this.mappingRepository = mappingRepository;
        this.storeRepository = storeRepository;
        this.competitionRepository = competitionRepository;
        this.aggregationService = aggregationService;
        this.policy = policy;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CategoryMappingRebuildResult rebuild(LocalDate requestedSnapshotDate) {
        LocalDate snapshotDate = snapshot(requestedSnapshotDate);
        List<SmallBusinessCategoryEntity> sources =
            sourceCategoryRepository.findByActiveTrue();
        List<BusinessCategoryEntity> targets =
            targetCategoryRepository.findByActiveTrue();
        Map<Long, BusinessCategoryEntity> targetsById = targets.stream()
            .collect(Collectors.toMap(
                BusinessCategoryEntity::getId,
                Function.identity()
            ));
        List<CategoryMappingCandidatePolicy.Target> policyTargets = targets.stream()
            .map(target -> new CategoryMappingCandidatePolicy.Target(
                target.getId(),
                target.getNormalizedCategoryName() == null
                    ? target.getSourceCategoryName()
                    : target.getNormalizedCategoryName()
            ))
            .toList();
        Map<String, Evidence> evidence = evidence(snapshotDate);
        Map<Long, CategoryMappingCandidateEntity> existing =
            candidateRepository.findAll().stream()
                .collect(Collectors.toMap(
                    candidate -> candidate.getSmallBusinessCategory().getId(),
                    Function.identity()
                ));

        mappingRepository.deleteByReviewType(MappingReviewType.AUTO);
        List<CategoryCodeMappingEntity> automaticMappings = new ArrayList<>();
        Map<String, ConfirmedSource> autoConfirmed = new HashMap<>();
        int confirmedCount = 0;
        int candidateCount = 0;
        int unresolvedCount = 0;
        for (SmallBusinessCategoryEntity source : sources) {
            Evidence sourceEvidence = evidence.getOrDefault(
                source.getSmallCategoryCode(),
                Evidence.empty()
            );
            var decision = policy.propose(
                source.getSmallCategoryName(),
                policyTargets
            );
            CategoryMappingCandidateEntity candidate = existing.getOrDefault(
                source.getId(),
                CategoryMappingCandidateEntity.create(source)
            );
            BusinessCategoryEntity target = decision.categoryId() == null
                ? null
                : targetsById.get(decision.categoryId());
            candidate.propose(
                target,
                decision.status(),
                decision.confidence(),
                decision.rule(),
                sourceEvidence.count(),
                serialize(sourceEvidence.ksicCodes())
            );
            switch (candidate.getMappingStatus()) {
                case AUTO_CONFIRMED -> {
                    confirmedCount++;
                    automaticMappings.add(CategoryCodeMappingEntity.create(
                        source.getSmallCategoryCode(),
                        null,
                        target,
                        decision.confidence(),
                        decision.rule(),
                        MappingStatus.AUTO_CONFIRMED,
                        MappingReviewType.AUTO,
                        null,
                        null
                    ));
                    autoConfirmed.put(
                        source.getSmallCategoryCode(),
                        new ConfirmedSource(target, sourceEvidence.ksicCodes())
                    );
                }
                case CONFIRMED -> confirmedCount++;
                case CANDIDATE -> candidateCount++;
                default -> unresolvedCount++;
            }
            candidateRepository.save(candidate);
        }
        automaticMappings.addAll(uniqueKsicMappings(autoConfirmed));
        mappingRepository.saveAll(automaticMappings);
        int remapped = remap(snapshotDate);
        return new CategoryMappingRebuildResult(
            snapshotDate,
            sources.size(),
            confirmedCount,
            candidateCount,
            unresolvedCount,
            remapped
        );
    }

    @Transactional(readOnly = true)
    public List<CategoryMappingCandidateResponse> list(
        MappingStatus status,
        int limit
    ) {
        PageRequest pageable = PageRequest.of(0, Math.max(1, Math.min(limit, 500)));
        var candidates = status == null
            ? candidateRepository.findAllByOrderByEvidenceCountDesc(pageable)
            : candidateRepository.findByMappingStatusOrderByEvidenceCountDesc(
                status,
                pageable
            );
        return candidates.stream()
            .map(CategoryMappingCandidateResponse::from)
            .toList();
    }

    @Transactional
    public CategoryMappingCandidateResponse confirm(
        Long candidateId,
        CategoryMappingReviewRequest request
    ) {
        if (request.businessCategoryId() == null) {
            throw new IllegalArgumentException("Business category is required.");
        }
        CategoryMappingCandidateEntity candidate = candidate(candidateId);
        BusinessCategoryEntity target = targetCategoryRepository
            .findById(request.businessCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Business category not found: " + request.businessCategoryId()
            ));
        candidate.confirm(target, request.reviewedBy(), request.note());
        String sourceCode = candidate.getSmallBusinessCategory()
            .getSmallCategoryCode();
        mappingRepository.deleteBySmallBusinessCategoryCode(sourceCode);
        mappingRepository.save(CategoryCodeMappingEntity.create(
            sourceCode,
            null,
            target,
            BigDecimal.ONE.setScale(4),
            "MANUAL_REVIEW",
            MappingStatus.CONFIRMED,
            MappingReviewType.MANUAL,
            request.reviewedBy(),
            request.note()
        ));
        LocalDate snapshotDate = storeRepository.findLatestSnapshotDate();
        if (snapshotDate != null) {
            remap(snapshotDate);
        }
        return CategoryMappingCandidateResponse.from(candidate);
    }

    @Transactional
    public CategoryMappingCandidateResponse reject(
        Long candidateId,
        CategoryMappingReviewRequest request
    ) {
        CategoryMappingCandidateEntity candidate = candidate(candidateId);
        candidate.reject(request.reviewedBy(), request.note());
        mappingRepository.deleteBySmallBusinessCategoryCode(
            candidate.getSmallBusinessCategory().getSmallCategoryCode()
        );
        LocalDate snapshotDate = storeRepository.findLatestSnapshotDate();
        if (snapshotDate != null) {
            remap(snapshotDate);
        }
        return CategoryMappingCandidateResponse.from(candidate);
    }

    private int remap(LocalDate snapshotDate) {
        storeRepository.clearCategoryMappings(snapshotDate);
        Map<String, BusinessCategoryEntity> targetsByCode =
            targetCategoryRepository.findByActiveTrue().stream()
                .filter(target -> target.getNormalizedCategoryCode() != null)
                .collect(Collectors.toMap(
                    BusinessCategoryEntity::getNormalizedCategoryCode,
                    Function.identity(),
                    (first, ignored) -> first
                ));
        int updated = 0;
        List<CategoryCodeMappingEntity> mappings = mappingRepository.findAll()
            .stream()
            .filter(CategoryCodeMappingEntity::isConfirmed)
            .toList();
        for (CategoryCodeMappingEntity mapping : mappings) {
            BusinessCategoryEntity target = targetsByCode.get(
                mapping.getNormalizedCategoryCode()
            );
            if (target == null || mapping.getSmallBusinessCategoryCode() == null) {
                continue;
            }
            updated += storeRepository.applySmallCategoryMapping(
                snapshotDate,
                mapping.getSmallBusinessCategoryCode(),
                target,
                mapping.getMappingConfidence(),
                mapping.getMappingRule()
            );
        }
        for (CategoryCodeMappingEntity mapping : mappings) {
            BusinessCategoryEntity target = targetsByCode.get(
                mapping.getNormalizedCategoryCode()
            );
            if (target == null || mapping.getKsicCode() == null) {
                continue;
            }
            updated += storeRepository.applyKsicMapping(
                snapshotDate,
                mapping.getKsicCode(),
                target,
                mapping.getMappingConfidence(),
                mapping.getMappingRule()
            );
        }
        competitionRepository.deleteBySnapshotDate(snapshotDate);
        aggregationService.aggregate(snapshotDate);
        return updated;
    }

    private Map<String, Evidence> evidence(LocalDate snapshotDate) {
        Map<String, MutableEvidence> grouped = new LinkedHashMap<>();
        storeRepository.aggregateCategoryCodeEvidence(snapshotDate)
            .forEach(row -> grouped.computeIfAbsent(
                row.getSourceCode(),
                ignored -> new MutableEvidence()
            ).add(row.getKsicCode(), row.getEvidenceCount()));
        return grouped.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> entry.getValue().toValue()
        ));
    }

    private List<CategoryCodeMappingEntity> uniqueKsicMappings(
        Map<String, ConfirmedSource> confirmedSources
    ) {
        Map<String, Set<Long>> targetIdsByKsic = new HashMap<>();
        Map<Long, BusinessCategoryEntity> targetsById = new HashMap<>();
        confirmedSources.values().forEach(source -> {
            targetsById.put(source.target().getId(), source.target());
            source.ksicCodes().forEach(ksic -> targetIdsByKsic
                .computeIfAbsent(ksic, ignored -> new HashSet<>())
                .add(source.target().getId()));
        });
        return targetIdsByKsic.entrySet().stream()
            .filter(entry -> entry.getValue().size() == 1)
            .map(entry -> CategoryCodeMappingEntity.create(
                null,
                entry.getKey(),
                targetsById.get(entry.getValue().iterator().next()),
                new BigDecimal("0.9000"),
                "UNIQUE_KSIC_FROM_AUTO_CONFIRMED_SOURCE",
                MappingStatus.AUTO_CONFIRMED,
                MappingReviewType.AUTO,
                null,
                null
            ))
            .toList();
    }

    private LocalDate snapshot(LocalDate requested) {
        LocalDate result = requested == null
            ? storeRepository.findLatestSnapshotDate()
            : requested;
        if (result == null) {
            throw new IllegalStateException("No store snapshot is available.");
        }
        return result;
    }

    private CategoryMappingCandidateEntity candidate(Long id) {
        return candidateRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Category mapping candidate not found: " + id
            ));
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot serialize KSIC evidence.", exception);
        }
    }

    private record Evidence(long count, Set<String> ksicCodes) {
        static Evidence empty() {
            return new Evidence(0, Set.of());
        }
    }

    private static final class MutableEvidence {
        private long count;
        private final Set<String> ksicCodes = new HashSet<>();

        void add(String ksicCode, long evidenceCount) {
            count += evidenceCount;
            if (ksicCode != null && !ksicCode.isBlank()) {
                ksicCodes.add(ksicCode);
            }
        }

        Evidence toValue() {
            return new Evidence(count, Set.copyOf(ksicCodes));
        }
    }

    private record ConfirmedSource(
        BusinessCategoryEntity target,
        Set<String> ksicCodes
    ) {
    }
}
