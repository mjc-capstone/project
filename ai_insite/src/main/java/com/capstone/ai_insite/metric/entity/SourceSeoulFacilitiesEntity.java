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
@Table(name = "source_seoul_facilities_by_dong_quarter")
public class SourceSeoulFacilitiesEntity
    extends AbstractSeoulRegionalSourceEntity<
        SeoulRegionalImportCommand.Facilities
    > {

    @Column(name = "facility_total_count")
    private Integer facilityTotalCount;
    @Column(name = "public_office_count")
    private Integer publicOfficeCount;
    @Column(name = "bank_count")
    private Integer bankCount;
    @Column(name = "general_hospital_count")
    private Integer generalHospitalCount;
    @Column(name = "hospital_count")
    private Integer hospitalCount;
    @Column(name = "pharmacy_count")
    private Integer pharmacyCount;
    @Column(name = "kindergarten_count")
    private Integer kindergartenCount;
    @Column(name = "elementary_school_count")
    private Integer elementarySchoolCount;
    @Column(name = "middle_school_count")
    private Integer middleSchoolCount;
    @Column(name = "high_school_count")
    private Integer highSchoolCount;
    @Column(name = "university_count")
    private Integer universityCount;
    @Column(name = "department_store_count")
    private Integer departmentStoreCount;
    @Column(name = "supermarket_count")
    private Integer supermarketCount;
    @Column(name = "theater_count")
    private Integer theaterCount;
    @Column(name = "lodging_count")
    private Integer lodgingCount;
    @Column(name = "railway_station_count")
    private Integer railwayStationCount;
    @Column(name = "subway_station_count")
    private Integer subwayStationCount;
    @Column(name = "bus_stop_count")
    private Integer busStopCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "facility_detail_json", columnDefinition = "json")
    private String facilityDetailJson;

    protected SourceSeoulFacilitiesEntity() {
    }

    public SourceSeoulFacilitiesEntity(
        RawApiPayloadEntity rawPayload,
        RegionEntity region,
        MetricPeriodEntity period
    ) {
        super(rawPayload, region, period);
    }

    @Override
    public void apply(
        SeoulRegionalImportCommand.Facilities command,
        RawApiPayloadEntity rawPayload
    ) {
        applyCommon(command, rawPayload);
        facilityTotalCount = command.totalCount();
        publicOfficeCount = command.publicOfficeCount();
        bankCount = command.bankCount();
        generalHospitalCount = command.generalHospitalCount();
        hospitalCount = command.hospitalCount();
        pharmacyCount = command.pharmacyCount();
        kindergartenCount = command.kindergartenCount();
        elementarySchoolCount = command.elementarySchoolCount();
        middleSchoolCount = command.middleSchoolCount();
        highSchoolCount = command.highSchoolCount();
        universityCount = command.universityCount();
        departmentStoreCount = command.departmentStoreCount();
        supermarketCount = command.supermarketCount();
        theaterCount = command.theaterCount();
        lodgingCount = command.lodgingCount();
        railwayStationCount = command.railwayStationCount();
        subwayStationCount = command.subwayStationCount();
        busStopCount = command.busStopCount();
        facilityDetailJson = command.detailJson();
    }
}
