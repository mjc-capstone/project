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
@Table(name = "source_seoul_resident_population_by_dong_quarter")
public class SourceSeoulResidentPopulationEntity
    extends AbstractSeoulRegionalSourceEntity<
        SeoulRegionalImportCommand.ResidentPopulation
    > {

    @Column(name = "resident_population_total")
    private Long residentPopulationTotal;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resident_population_by_gender_json", columnDefinition = "json")
    private String residentPopulationByGenderJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resident_population_by_age_json", columnDefinition = "json")
    private String residentPopulationByAgeJson;

    @Column(name = "household_count")
    private Long householdCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "household_type_json", columnDefinition = "json")
    private String householdTypeJson;

    protected SourceSeoulResidentPopulationEntity() {
    }

    public SourceSeoulResidentPopulationEntity(
        RawApiPayloadEntity rawPayload,
        RegionEntity region,
        MetricPeriodEntity period
    ) {
        super(rawPayload, region, period);
    }

    @Override
    public void apply(
        SeoulRegionalImportCommand.ResidentPopulation command,
        RawApiPayloadEntity rawPayload
    ) {
        applyCommon(command, rawPayload);
        residentPopulationTotal = command.total();
        residentPopulationByGenderJson = command.byGenderJson();
        residentPopulationByAgeJson = command.byAgeJson();
        householdCount = command.householdCount();
        householdTypeJson = command.householdTypeJson();
    }
}
