package com.capstone.ai_insite.metric.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "metric_periods")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MetricPeriodEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "period_code", nullable = false, length = 20)
    private String periodCode;

    @Column(name = "period_type", nullable = false, length = 20)
    private String periodType;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "quarter")
    private Integer quarter;

    @Column(name = "month")
    private Integer month;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    public static MetricPeriodEntity createQuarter(
        String periodCode,
        int year,
        int quarter
    ) {
        if (quarter < 1 || quarter > 4) {
            throw new IllegalArgumentException("분기는 1~4 범위여야 합니다.");
        }
        MetricPeriodEntity entity = new MetricPeriodEntity();
        entity.periodCode = periodCode;
        entity.periodType = "QUARTER";
        entity.year = year;
        entity.quarter = quarter;
        entity.startDate = LocalDate.of(year, (quarter - 1) * 3 + 1, 1);
        entity.endDate = entity.startDate.plusMonths(3).minusDays(1);
        return entity;
    }
}
