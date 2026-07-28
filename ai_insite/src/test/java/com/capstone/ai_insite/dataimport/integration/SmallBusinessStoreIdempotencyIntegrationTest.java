package com.capstone.ai_insite.dataimport.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.capstone.ai_insite.category.entity.BusinessCategoryEntity;
import com.capstone.ai_insite.category.entity.CategoryCodeMappingEntity;
import com.capstone.ai_insite.category.domain.MappingReviewType;
import com.capstone.ai_insite.category.domain.MappingStatus;
import com.capstone.ai_insite.category.repository.BusinessCategoryJpaRepository;
import com.capstone.ai_insite.category.repository.CategoryCodeMappingJpaRepository;
import com.capstone.ai_insite.dataimport.client.PublicDataApiClient;
import com.capstone.ai_insite.dataimport.repository.RawApiPayloadJpaRepository;
import com.capstone.ai_insite.dataimport.repository.SourceSmallBusinessStoreJpaRepository;
import com.capstone.ai_insite.dataimport.service.SmallBusinessStoreDataImportService;
import com.capstone.ai_insite.metric.service.NearbyCompetitionQueryService;
import com.capstone.ai_insite.region.entity.RegionEntity;
import com.capstone.ai_insite.region.repository.RegionJpaRepository;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@SpringBootTest(properties = {
    "external.public-data.enabled=true",
    "external.public-data.small-business.page-size=10",
    "external.public-data.small-business.max-pages-per-district=2"
})
@Import(SmallBusinessStoreIdempotencyIntegrationTest.FakeApiConfiguration.class)
class SmallBusinessStoreIdempotencyIntegrationTest {

    private static final LocalDate SNAPSHOT_DATE = LocalDate.of(2099, 12, 31);

    @Autowired
    private SmallBusinessStoreDataImportService importService;
    @Autowired
    private RegionJpaRepository regionRepository;
    @Autowired
    private BusinessCategoryJpaRepository categoryRepository;
    @Autowired
    private CategoryCodeMappingJpaRepository categoryMappingRepository;
    @Autowired
    private SourceSmallBusinessStoreJpaRepository storeRepository;
    @Autowired
    private RawApiPayloadJpaRepository rawPayloadRepository;
    @Autowired
    private NearbyCompetitionQueryService nearbyCompetitionQueryService;

    @BeforeEach
    void seedMappingTargets() {
        regionRepository.findByAdministrativeDongCode("11110615")
            .orElseGet(() -> regionRepository.save(
                RegionEntity.createSeoulAdministrativeDong(
                    "11110615",
                    "TEST_DONG",
                    "11110",
                    "TEST_DISTRICT"
                )
            ));
        BusinessCategoryEntity target = categoryRepository
            .findBySourceSystemAndSourceCategoryCode(
            BusinessCategoryEntity.SEOUL_COMMERCIAL_SOURCE,
            "TEST_CATEGORY_CODE"
        ).orElseGet(() -> categoryRepository.save(
            BusinessCategoryEntity.createSeoulCommercial(
                "TEST_CATEGORY_CODE",
                "TEST_CATEGORY"
            )
        ));
        categoryMappingRepository.deleteBySmallBusinessCategoryCode("TEST_S");
        categoryMappingRepository.save(CategoryCodeMappingEntity.create(
            "TEST_S",
            null,
            target,
            new BigDecimal("1.0000"),
            "TEST_CONFIRMED_MAPPING",
            MappingStatus.CONFIRMED,
            MappingReviewType.MANUAL,
            "integration-test",
            null
        ));
    }

    @Test
    void repeatedSnapshotCollectionUpsertsInsteadOfDuplicating() {
        var first = importService.collect("11110", "idempotency-test");
        long countAfterFirst = storeRepository.countBySnapshotDate(SNAPSHOT_DATE);
        var second = importService.collect("11110", "idempotency-test");
        Long categoryId = categoryRepository.findBySourceSystemAndSourceCategoryCode(
            BusinessCategoryEntity.SEOUL_COMMERCIAL_SOURCE,
            "TEST_CATEGORY_CODE"
        ).orElseThrow().getId();
        var nearby = nearbyCompetitionQueryService.find(
            new BigDecimal("37.58000000"),
            new BigDecimal("126.97000000"),
            categoryId,
            300,
            SNAPSHOT_DATE
        );

        assertEquals(1, countAfterFirst);
        assertEquals(1, storeRepository.countBySnapshotDate(SNAPSHOT_DATE));
        assertEquals(1, rawPayloadRepository.countByDataImportJobId(first.jobId()));
        assertEquals(1, rawPayloadRepository.countByDataImportJobId(second.jobId()));
        assertEquals(1, first.normalizedRowCount());
        assertEquals(1, second.normalizedRowCount());
        assertEquals(1, nearby.nearbyStoreCount());
        assertEquals(1, nearby.sameCategoryStoreCount());
    }

    @TestConfiguration
    static class FakeApiConfiguration {

        @Bean
        @Primary
        PublicDataApiClient fakePublicDataApiClient() {
            return new PublicDataApiClient() {
                @Override
                public String fetch(
                    String path,
                    Map<String, String> queryParameters
                ) {
                    return """
                        {
                          "header": {
                            "resultCode": "00",
                            "resultMsg": "NORMAL SERVICE",
                            "stdrYm": "209912"
                          },
                          "body": {
                            "items": [{
                              "bizesId": "IDEMPOTENCY-STORE-1",
                              "bizesNm": "TEST_STORE",
                              "indsLclsCd": "TEST_L",
                              "indsLclsNm": "TEST_LARGE",
                              "indsMclsCd": "TEST_M",
                              "indsMclsNm": "TEST_MEDIUM",
                              "indsSclsCd": "TEST_S",
                              "indsSclsNm": "TEST_CATEGORY",
                              "ksicCd": "TEST_KSIC",
                              "ksicNm": "TEST_KSIC_NAME",
                              "adongCd": "11110615",
                              "ldongCd": "1111010100",
                              "lnoAdr": "TEST_JIBUN",
                              "rdnmAdr": "TEST_ROAD",
                              "lon": 126.97000000,
                              "lat": 37.58000000
                            }],
                            "numOfRows": 1,
                            "pageNo": 1,
                            "totalCount": 1
                          }
                        }
                        """;
                }
            };
        }
    }
}
