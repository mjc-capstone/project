package com.capstone.ai_insite.metric.entity;

import com.capstone.ai_insite.dataimport.domain.SeoulRegionalImportCommand;
import com.capstone.ai_insite.dataimport.entity.RawApiPayloadEntity;
import com.capstone.ai_insite.region.entity.RegionEntity;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@MappedSuperclass
public abstract class AbstractSeoulRegionalSourceEntity<
    C extends SeoulRegionalImportCommand
> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raw_api_payload_id")
    private RawApiPayloadEntity rawApiPayload;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private RegionEntity region;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metric_period_id", nullable = false)
    private MetricPeriodEntity metricPeriod;

    @Column(name = "source_period_code", nullable = false, length = 20)
    private String sourcePeriodCode;

    @Column(name = "region_code_snapshot", nullable = false, length = 20)
    private String regionCodeSnapshot;

    @Column(name = "region_name_snapshot", length = 50)
    private String regionNameSnapshot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "source_row_json", columnDefinition = "json")
    private String sourceRowJson;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    protected AbstractSeoulRegionalSourceEntity() {
    }

    protected AbstractSeoulRegionalSourceEntity(
        RawApiPayloadEntity rawApiPayload,
        RegionEntity region,
        MetricPeriodEntity metricPeriod
    ) {
        this.rawApiPayload = rawApiPayload;
        this.region = region;
        this.metricPeriod = metricPeriod;
    }

    protected void applyCommon(C command, RawApiPayloadEntity rawPayload) {
        this.rawApiPayload = rawPayload;
        this.sourcePeriodCode = command.sourcePeriodCode();
        this.regionCodeSnapshot = command.regionCode();
        this.regionNameSnapshot = command.regionName();
        this.sourceRowJson = command.sourceRowJson();
    }

    public abstract void apply(C command, RawApiPayloadEntity rawPayload);
}
