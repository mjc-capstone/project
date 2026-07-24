package com.capstone.ai_insite.metric.entity;

import com.capstone.ai_insite.dataimport.domain.SeoulRegionalImportCommand;
import com.capstone.ai_insite.dataimport.entity.RawApiPayloadEntity;
import com.capstone.ai_insite.region.entity.RegionEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "source_seoul_apartments_by_dong_quarter")
public class SourceSeoulApartmentsEntity
    extends AbstractSeoulRegionalSourceEntity<
        SeoulRegionalImportCommand.Apartments
    > {

    @Column(name = "apartment_complex_count")
    private Integer apartmentComplexCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "apartment_households_by_area_json", columnDefinition = "json")
    private String apartmentHouseholdsByAreaJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "apartment_households_by_price_json", columnDefinition = "json")
    private String apartmentHouseholdsByPriceJson;

    @Column(name = "avg_apartment_area", precision = 12, scale = 2)
    private BigDecimal averageApartmentArea;

    @Column(name = "avg_apartment_market_price")
    private Long averageApartmentMarketPrice;

    protected SourceSeoulApartmentsEntity() {
    }

    public SourceSeoulApartmentsEntity(
        RawApiPayloadEntity rawPayload,
        RegionEntity region,
        MetricPeriodEntity period
    ) {
        super(rawPayload, region, period);
    }

    @Override
    public void apply(
        SeoulRegionalImportCommand.Apartments command,
        RawApiPayloadEntity rawPayload
    ) {
        applyCommon(command, rawPayload);
        apartmentComplexCount = command.complexCount();
        apartmentHouseholdsByAreaJson = command.householdsByAreaJson();
        apartmentHouseholdsByPriceJson = command.householdsByPriceJson();
        averageApartmentArea = command.averageArea();
        averageApartmentMarketPrice = command.averageMarketPrice();
    }
}
