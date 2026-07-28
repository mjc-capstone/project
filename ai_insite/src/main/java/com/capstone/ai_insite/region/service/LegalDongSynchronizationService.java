package com.capstone.ai_insite.region.service;

import com.capstone.ai_insite.dataimport.dto.publicdata.StandardLegalDongRow;
import com.capstone.ai_insite.region.entity.LegalDongEntity;
import com.capstone.ai_insite.region.repository.LegalDongJpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LegalDongSynchronizationService {

    private final LegalDongJpaRepository repository;

    public LegalDongSynchronizationService(LegalDongJpaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public int synchronize(
        List<StandardLegalDongRow> rows,
        LocalDate sourceReferenceDate
    ) {
        Set<String> sourceCodes = rows.stream()
            .map(StandardLegalDongRow::legalDongCode)
            .collect(Collectors.toSet());
        Map<String, LegalDongEntity> existing = repository
            .findByLegalDongCodeIn(sourceCodes)
            .stream()
            .collect(Collectors.toMap(
                LegalDongEntity::getLegalDongCode,
                Function.identity()
            ));
        List<LegalDongEntity> changed = rows.stream().map(row -> {
            LegalDongEntity entity = existing.getOrDefault(
                row.legalDongCode(),
                LegalDongEntity.create(row.legalDongCode())
            );
            entity.synchronize(
                row.legalDongCode(),
                row.sidoCode(),
                row.sidoName(),
                row.sigunguCode(),
                row.sigunguName(),
                row.legalDongName(),
                row.effectiveFrom(),
                sourceReferenceDate
            );
            return entity;
        }).toList();
        repository.saveAll(changed);
        repository.findBySourceSystemAndActiveTrue(
            "MOIS_STANDARD_REGION_CODE"
        ).stream()
            .filter(entity -> !sourceCodes.contains(entity.getLegalDongCode()))
            .forEach(entity -> entity.deactivate(sourceReferenceDate.minusDays(1)));
        return changed.size();
    }
}
