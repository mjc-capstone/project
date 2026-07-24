package com.capstone.ai_insite.metric.entity;

import com.capstone.ai_insite.dataimport.domain.SeoulRegionalImportCommand;
import com.capstone.ai_insite.dataimport.entity.RawApiPayloadEntity;
import com.capstone.ai_insite.region.entity.RegionEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "source_seoul_working_population_by_dong_quarter")
public class SourceSeoulWorkingPopulationEntity
    extends AbstractSeoulRegionalSourceEntity<
        SeoulRegionalImportCommand.WorkingPopulation
    > {

    @Column(name = "working_population_total")
    private Long workingPopulationTotal;

    @Column(name = "working_population_male")
    private Long workingPopulationMale;

    @Column(name = "working_population_female")
    private Long workingPopulationFemale;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "working_population_by_age_json", columnDefinition = "json")
    private String workingPopulationByAgeJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "working_population_male_by_age_json", columnDefinition = "json")
    private String workingPopulationMaleByAgeJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "working_population_female_by_age_json", columnDefinition = "json")
    private String workingPopulationFemaleByAgeJson;

    protected SourceSeoulWorkingPopulationEntity() {
    }

    public SourceSeoulWorkingPopulationEntity(
        RawApiPayloadEntity rawPayload,
        RegionEntity region,
        MetricPeriodEntity period
    ) {
        super(rawPayload, region, period);
    }

    @Override
    public void apply(
        SeoulRegionalImportCommand.WorkingPopulation command,
        RawApiPayloadEntity rawPayload
    ) {
        applyCommon(command, rawPayload);
        workingPopulationTotal = command.total();
        workingPopulationMale = command.male();
        workingPopulationFemale = command.female();
        workingPopulationByAgeJson = command.byAgeJson();
        workingPopulationMaleByAgeJson = command.maleByAgeJson();
        workingPopulationFemaleByAgeJson = command.femaleByAgeJson();
    }
}
