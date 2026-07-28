package com.capstone.ai_insite.dataimport.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.capstone.ai_insite.category.repository.CategoryMappingCandidateJpaRepository;
import com.capstone.ai_insite.category.repository.SmallBusinessCategoryJpaRepository;
import com.capstone.ai_insite.dataimport.service.CodeMappingDataImportService;
import com.capstone.ai_insite.region.repository.AdministrativeLegalDongMappingJpaRepository;
import com.capstone.ai_insite.region.repository.LegalDongJpaRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "external.public-data.enabled=true")
@EnabledIfEnvironmentVariable(
    named = "RUN_CODE_MAPPING_API_INTEGRATION",
    matches = "true"
)
class CodeMappingDataImportIntegrationTest {

    @Autowired
    private CodeMappingDataImportService importService;
    @Autowired
    private LegalDongJpaRepository legalDongRepository;
    @Autowired
    private SmallBusinessCategoryJpaRepository categoryRepository;
    @Autowired
    private CategoryMappingCandidateJpaRepository candidateRepository;
    @Autowired
    private AdministrativeLegalDongMappingJpaRepository regionMappingRepository;

    @Test
    void synchronizesOfficialCodeMastersAndBuildsReviewableMappings() {
        var result = importService.synchronize(
            LocalDate.of(2026, 3, 31),
            "live-code-mapping-test"
        );

        assertTrue(result.synchronizedLegalDongCount() > 400);
        assertTrue(result.synchronizedSmallCategoryCount() >= 200);
        assertEquals(
            result.synchronizedLegalDongCount(),
            legalDongRepository.findByActiveTrue().size()
        );
        assertEquals(
            result.synchronizedSmallCategoryCount(),
            categoryRepository.findByActiveTrue().size()
        );
        assertEquals(
            result.synchronizedSmallCategoryCount(),
            candidateRepository.count()
        );
        assertTrue(regionMappingRepository.count() > 0);
        assertTrue(result.categoryMappings().autoConfirmedCount() > 0);
    }
}
