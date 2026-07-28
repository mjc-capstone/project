package com.capstone.ai_insite.dataimport.repository;

import com.capstone.ai_insite.dataimport.entity.SourceSmallBusinessStoreEntity;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

public interface SourceSmallBusinessStoreJpaRepository
    extends JpaRepository<SourceSmallBusinessStoreEntity, Long> {

    List<SourceSmallBusinessStoreEntity>
        findByExternalStoreIdInAndSnapshotDate(
            Collection<String> externalStoreIds,
            LocalDate snapshotDate
        );

    long countBySnapshotDate(LocalDate snapshotDate);

    @Query("""
        select max(s.snapshotDate)
        from SourceSmallBusinessStoreEntity s
        where s.snapshotDate <= current_date
        """)
    LocalDate findLatestSnapshotDate();

    @Query("""
        select s from SourceSmallBusinessStoreEntity s
        where s.snapshotDate = :snapshotDate
          and s.region is not null
          and s.businessCategory is not null
        """)
    List<SourceSmallBusinessStoreEntity> findMappedBySnapshotDate(
        @Param("snapshotDate") LocalDate snapshotDate
    );

    @Query("""
        select s.region.id as regionId,
               s.businessCategory.id as categoryId,
               count(s.id) as storeCount
        from SourceSmallBusinessStoreEntity s
        where s.snapshotDate = :snapshotDate
          and s.region is not null
          and s.businessCategory is not null
        group by s.region.id, s.businessCategory.id
        """)
    List<StoreCategoryAggregate> aggregateByRegionAndCategory(
        @Param("snapshotDate") LocalDate snapshotDate
    );

    @Query("""
        select s.region.id as regionId,
               count(s.id) as storeCount
        from SourceSmallBusinessStoreEntity s
        where s.snapshotDate = :snapshotDate
          and s.region is not null
          and s.businessCategory is not null
        group by s.region.id
        """)
    List<StoreRegionAggregate> aggregateByRegion(
        @Param("snapshotDate") LocalDate snapshotDate
    );

    @Query("""
        select s.sourceSmallCategoryCode as sourceCode,
               max(s.sourceSmallCategoryName) as sourceName,
               count(s.id) as rowCount
        from SourceSmallBusinessStoreEntity s
        where s.snapshotDate = :snapshotDate
          and s.businessCategory is null
          and s.sourceSmallCategoryCode is not null
        group by s.sourceSmallCategoryCode
        order by count(s.id) desc
        """)
    List<UnmappedCodeAggregate> aggregateUnmappedCategories(
        @Param("snapshotDate") LocalDate snapshotDate
    );

    @Query("""
        select s.administrativeDongCode as sourceCode,
               count(s.id) as rowCount
        from SourceSmallBusinessStoreEntity s
        where s.snapshotDate = :snapshotDate
          and s.region is null
          and s.administrativeDongCode is not null
        group by s.administrativeDongCode
        order by count(s.id) desc
        """)
    List<UnmappedRegionAggregate> aggregateUnmappedRegions(
        @Param("snapshotDate") LocalDate snapshotDate
    );

    @Query("""
        select s from SourceSmallBusinessStoreEntity s
        where s.snapshotDate = :snapshotDate
          and s.latitude between :minLatitude and :maxLatitude
          and s.longitude between :minLongitude and :maxLongitude
        """)
    List<SourceSmallBusinessStoreEntity> findLocationCandidates(
        @Param("snapshotDate") LocalDate snapshotDate,
        @Param("minLatitude") java.math.BigDecimal minLatitude,
        @Param("maxLatitude") java.math.BigDecimal maxLatitude,
        @Param("minLongitude") java.math.BigDecimal minLongitude,
        @Param("maxLongitude") java.math.BigDecimal maxLongitude
    );

    @Query("""
        select s.administrativeDongCode as administrativeDongCode,
               s.legalDongCode as legalDongCode,
               count(s.id) as evidenceCount
        from SourceSmallBusinessStoreEntity s
        where s.snapshotDate = :snapshotDate
          and s.administrativeDongCode is not null
          and s.legalDongCode is not null
        group by s.administrativeDongCode, s.legalDongCode
        """)
    List<RegionCodePairEvidence> aggregateRegionCodePairs(
        @Param("snapshotDate") LocalDate snapshotDate
    );

    @Query("""
        select s.sourceSmallCategoryCode as sourceCode,
               max(s.sourceSmallCategoryName) as sourceName,
               s.ksicCode as ksicCode,
               max(s.ksicName) as ksicName,
               count(s.id) as evidenceCount
        from SourceSmallBusinessStoreEntity s
        where s.snapshotDate = :snapshotDate
          and s.sourceSmallCategoryCode is not null
        group by s.sourceSmallCategoryCode, s.ksicCode
        """)
    List<CategoryCodeEvidence> aggregateCategoryCodeEvidence(
        @Param("snapshotDate") LocalDate snapshotDate
    );

    @Modifying
    @Query(value = """
        UPDATE source_small_business_stores s
        JOIN legal_dongs l ON l.legal_dong_code = s.legal_dong_code
        SET s.legal_dong_id = l.id
        WHERE s.snapshot_date = :snapshotDate
        """, nativeQuery = true)
    int linkLegalDongs(@Param("snapshotDate") LocalDate snapshotDate);

    @Modifying
    @Query("""
        update SourceSmallBusinessStoreEntity s
        set s.businessCategory = null,
            s.categoryMappingConfidence = null,
            s.categoryMappingRule = null
        where s.snapshotDate = :snapshotDate
        """)
    int clearCategoryMappings(@Param("snapshotDate") LocalDate snapshotDate);

    @Modifying
    @Query("""
        update SourceSmallBusinessStoreEntity s
        set s.businessCategory = :category,
            s.categoryMappingConfidence = :confidence,
            s.categoryMappingRule = :rule
        where s.snapshotDate = :snapshotDate
          and s.sourceSmallCategoryCode = :sourceCode
        """)
    int applySmallCategoryMapping(
        @Param("snapshotDate") LocalDate snapshotDate,
        @Param("sourceCode") String sourceCode,
        @Param("category")
        com.capstone.ai_insite.category.entity.BusinessCategoryEntity category,
        @Param("confidence") java.math.BigDecimal confidence,
        @Param("rule") String rule
    );

    @Modifying
    @Query("""
        update SourceSmallBusinessStoreEntity s
        set s.businessCategory = :category,
            s.categoryMappingConfidence = :confidence,
            s.categoryMappingRule = :rule
        where s.snapshotDate = :snapshotDate
          and s.businessCategory is null
          and s.ksicCode = :ksicCode
        """)
    int applyKsicMapping(
        @Param("snapshotDate") LocalDate snapshotDate,
        @Param("ksicCode") String ksicCode,
        @Param("category")
        com.capstone.ai_insite.category.entity.BusinessCategoryEntity category,
        @Param("confidence") java.math.BigDecimal confidence,
        @Param("rule") String rule
    );

    interface StoreCategoryAggregate {
        Long getRegionId();
        Long getCategoryId();
        long getStoreCount();
    }

    interface StoreRegionAggregate {
        Long getRegionId();
        long getStoreCount();
    }

    interface UnmappedCodeAggregate {
        String getSourceCode();
        String getSourceName();
        long getRowCount();
    }

    interface UnmappedRegionAggregate {
        String getSourceCode();
        long getRowCount();
    }

    interface RegionCodePairEvidence {
        String getAdministrativeDongCode();
        String getLegalDongCode();
        long getEvidenceCount();
    }

    interface CategoryCodeEvidence {
        String getSourceCode();
        String getSourceName();
        String getKsicCode();
        String getKsicName();
        long getEvidenceCount();
    }
}
