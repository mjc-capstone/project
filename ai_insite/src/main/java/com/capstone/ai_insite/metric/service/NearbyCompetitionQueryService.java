package com.capstone.ai_insite.metric.service;

import com.capstone.ai_insite.category.repository.BusinessCategoryJpaRepository;
import com.capstone.ai_insite.common.exception.ResourceNotFoundException;
import com.capstone.ai_insite.dataimport.entity.SourceSmallBusinessStoreEntity;
import com.capstone.ai_insite.dataimport.repository.SourceSmallBusinessStoreJpaRepository;
import com.capstone.ai_insite.metric.domain.policy.CompetitionRadiusPolicy;
import com.capstone.ai_insite.metric.dto.NearbyCompetitionResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NearbyCompetitionQueryService {

    private static final BigDecimal METERS_PER_LATITUDE_DEGREE =
        new BigDecimal("111320");

    private final SourceSmallBusinessStoreJpaRepository storeRepository;
    private final BusinessCategoryJpaRepository categoryRepository;
    private final CompetitionRadiusPolicy radiusPolicy;

    public NearbyCompetitionQueryService(
        SourceSmallBusinessStoreJpaRepository storeRepository,
        BusinessCategoryJpaRepository categoryRepository,
        CompetitionRadiusPolicy radiusPolicy
    ) {
        this.storeRepository = storeRepository;
        this.categoryRepository = categoryRepository;
        this.radiusPolicy = radiusPolicy;
    }

    @Transactional(readOnly = true)
    public NearbyCompetitionResponse find(
        BigDecimal latitude,
        BigDecimal longitude,
        Long businessCategoryId,
        int radiusMeters,
        LocalDate snapshotDate
    ) {
        validate(latitude, longitude, radiusMeters);
        if (businessCategoryId == null) {
            throw new IllegalArgumentException("Business category is required.");
        }
        if (!categoryRepository.existsById(businessCategoryId)) {
            throw new ResourceNotFoundException(
                "Business category not found: " + businessCategoryId
            );
        }
        LocalDate target = snapshotDate == null
            ? storeRepository.findLatestSnapshotDate()
            : snapshotDate;
        if (target == null) {
            return new NearbyCompetitionResponse(
                null,
                radiusMeters,
                0,
                0,
                0,
                0
            );
        }
        BigDecimal latitudeDelta = BigDecimal.valueOf(radiusMeters)
            .divide(METERS_PER_LATITUDE_DEGREE, 10, java.math.RoundingMode.CEILING);
        double longitudeMetersPerDegree = 111_320.0 * Math.max(
            Math.abs(Math.cos(Math.toRadians(latitude.doubleValue()))),
            0.000001
        );
        BigDecimal longitudeDelta = BigDecimal.valueOf(
            radiusMeters / longitudeMetersPerDegree
        );
        List<SourceSmallBusinessStoreEntity> stores =
            storeRepository.findLocationCandidates(
                target,
                latitude.subtract(latitudeDelta),
                latitude.add(latitudeDelta),
                longitude.subtract(longitudeDelta),
                longitude.add(longitudeDelta)
            ).stream()
                .filter(store -> radiusPolicy.isWithinMeters(
                    latitude,
                    longitude,
                    store.getLatitude(),
                    store.getLongitude(),
                    radiusMeters
                ))
                .toList();

        int sameCategory = Math.toIntExact(stores.stream()
            .filter(store -> store.getBusinessCategory() != null)
            .filter(store -> businessCategoryId.equals(
                store.getBusinessCategory().getId()
            ))
            .count());
        int mappedCategoryCount = Math.toIntExact(stores.stream()
            .map(SourceSmallBusinessStoreEntity::getBusinessCategory)
            .filter(Objects::nonNull)
            .map(category -> category.getId())
            .distinct()
            .count());
        int unmapped = Math.toIntExact(stores.stream()
            .filter(store -> store.getBusinessCategory() == null)
            .count());
        return new NearbyCompetitionResponse(
            target,
            radiusMeters,
            stores.size(),
            sameCategory,
            mappedCategoryCount,
            unmapped
        );
    }

    private static void validate(
        BigDecimal latitude,
        BigDecimal longitude,
        int radiusMeters
    ) {
        if (latitude == null
            || latitude.compareTo(new BigDecimal("-90")) < 0
            || latitude.compareTo(new BigDecimal("90")) > 0) {
            throw new IllegalArgumentException("Latitude is out of range.");
        }
        if (longitude == null
            || longitude.compareTo(new BigDecimal("-180")) < 0
            || longitude.compareTo(new BigDecimal("180")) > 0) {
            throw new IllegalArgumentException("Longitude is out of range.");
        }
        if (radiusMeters != 300 && radiusMeters != 500) {
            throw new IllegalArgumentException("Radius must be 300 or 500 meters.");
        }
    }
}
