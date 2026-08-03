package com.capstone.ai_insite.dataimport.service;

import com.capstone.ai_insite.dataimport.dto.publicdata.CollectedCommercialTransaction;
import com.capstone.ai_insite.dataimport.dto.reb.CollectedRebCommercialRentObservation;
import com.capstone.ai_insite.metric.entity.MetricPeriodEntity;
import com.capstone.ai_insite.metric.entity.SourceMolitCommercialTransactionEntity;
import com.capstone.ai_insite.metric.entity.SourceRebCommercialRentStatEntity;
import com.capstone.ai_insite.metric.repository.SourceMolitCommercialTransactionJpaRepository;
import com.capstone.ai_insite.metric.repository.SourceRebCommercialRentStatJpaRepository;
import com.capstone.ai_insite.region.entity.LegalDongEntity;
import com.capstone.ai_insite.region.repository.LegalDongJpaRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CostSourcePersistenceService {

    private final SourceRebCommercialRentStatJpaRepository rebRepository;
    private final SourceMolitCommercialTransactionJpaRepository transactionRepository;
    private final LegalDongJpaRepository legalDongRepository;

    public CostSourcePersistenceService(
        SourceRebCommercialRentStatJpaRepository rebRepository,
        SourceMolitCommercialTransactionJpaRepository transactionRepository,
        LegalDongJpaRepository legalDongRepository
    ) {
        this.rebRepository = rebRepository;
        this.transactionRepository = transactionRepository;
        this.legalDongRepository = legalDongRepository;
    }

    @Transactional
    public int replaceReb(
        MetricPeriodEntity period,
        List<CollectedRebCommercialRentObservation> collected
    ) {
        rebRepository.deleteByMetricPeriodId(period.getId());
        List<SourceRebCommercialRentStatEntity> entities = collected.stream()
            .map(value -> new SourceRebCommercialRentStatEntity(
                value.rawApiPayload(),
                period,
                value.observation()
            ))
            .toList();
        return rebRepository.saveAll(entities).size();
    }

    @Transactional
    public int replaceTransactions(
        MetricPeriodEntity period,
        List<String> districtCodes,
        List<CollectedCommercialTransaction> collected
    ) {
        districtCodes.forEach(districtCode ->
            transactionRepository.deleteByDistrictCodeAndDealDateBetween(
                districtCode,
                period.getStartDate(),
                period.getEndDate()
            )
        );
        Map<String, Map<String, LegalDongEntity>> legalDongsByDistrict =
            legalDongs(districtCodes);
        Map<String, Integer> occurrences = new HashMap<>();
        List<SourceMolitCommercialTransactionEntity> entities = collected.stream()
            .map(value -> {
                String signature = sha256(value.row().sourceRowJson());
                String occurrenceKey = value.row().districtCode()
                    + "|" + value.row().dealDate() + "|" + signature;
                int occurrenceNo = occurrences.merge(
                    occurrenceKey,
                    1,
                    Integer::sum
                );
                LegalDongEntity legalDong = legalDongsByDistrict
                    .getOrDefault(value.row().districtCode(), Map.of())
                    .get(value.row().legalDongName());
                return new SourceMolitCommercialTransactionEntity(
                    value.rawApiPayload(),
                    legalDong,
                    value.row(),
                    signature,
                    occurrenceNo
                );
            })
            .toList();
        return transactionRepository.saveAll(entities).size();
    }

    private Map<String, Map<String, LegalDongEntity>> legalDongs(
        List<String> districtCodes
    ) {
        Map<String, Map<String, LegalDongEntity>> result = new LinkedHashMap<>();
        for (String districtCode : districtCodes) {
            result.put(
                districtCode,
                legalDongRepository.findBySigunguCodeAndActiveTrue(districtCode)
                    .stream()
                    .collect(Collectors.toMap(
                        LegalDongEntity::getLegalDongName,
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new
                    ))
            );
        }
        return result;
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception exception) {
            throw new IllegalStateException(
                "Transaction signature generation failed.",
                exception
            );
        }
    }
}
