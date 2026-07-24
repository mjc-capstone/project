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
@Table(name = "source_seoul_floating_population_by_dong_quarter")
public class SourceSeoulFloatingPopulationEntity
    extends AbstractSeoulRegionalSourceEntity<
        SeoulRegionalImportCommand.FloatingPopulation
    > {

    @Column(name = "floating_population_total")
    private Long floatingPopulationTotal;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "floating_population_by_gender_json", columnDefinition = "json")
    private String floatingPopulationByGenderJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "floating_population_by_age_json", columnDefinition = "json")
    private String floatingPopulationByAgeJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "floating_population_by_day_json", columnDefinition = "json")
    private String floatingPopulationByDayJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "floating_population_by_time_json", columnDefinition = "json")
    private String floatingPopulationByTimeJson;

    protected SourceSeoulFloatingPopulationEntity() {
    }

    public SourceSeoulFloatingPopulationEntity(
        RawApiPayloadEntity rawPayload,
        RegionEntity region,
        MetricPeriodEntity period
    ) {
        super(rawPayload, region, period);
    }

    @Override
    public void apply(
        SeoulRegionalImportCommand.FloatingPopulation command,
        RawApiPayloadEntity rawPayload
    ) {
        applyCommon(command, rawPayload);
        floatingPopulationTotal = command.total();
        floatingPopulationByGenderJson = command.byGenderJson();
        floatingPopulationByAgeJson = command.byAgeJson();
        floatingPopulationByDayJson = command.byDayJson();
        floatingPopulationByTimeJson = command.byTimeJson();
    }
}
