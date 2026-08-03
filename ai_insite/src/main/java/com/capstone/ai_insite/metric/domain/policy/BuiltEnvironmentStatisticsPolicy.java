package com.capstone.ai_insite.metric.domain.policy;

import com.capstone.ai_insite.metric.domain.BuildingObservation;
import com.capstone.ai_insite.metric.domain.BuiltEnvironmentStatistics;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

public class BuiltEnvironmentStatisticsPolicy {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final long AGED_BUILDING_YEARS = 30;

    private final BuildingUsePolicy buildingUsePolicy;

    public BuiltEnvironmentStatisticsPolicy(BuildingUsePolicy buildingUsePolicy) {
        this.buildingUsePolicy = buildingUsePolicy;
    }

    public BuiltEnvironmentStatistics calculate(
        List<BuildingObservation> observations,
        LocalDate snapshotDate
    ) {
        List<BuildingObservation> commercial = observations.stream()
            .filter(value -> buildingUsePolicy.isCommercial(value.mainUseCode()))
            .toList();
        List<BigDecimal> ages = commercial.stream()
            .map(value -> age(value.approvalDate(), snapshotDate))
            .filter(Objects::nonNull)
            .toList();
        List<BigDecimal> areas = commercial.stream()
            .map(BuildingObservation::grossFloorArea)
            .filter(BuiltEnvironmentStatisticsPolicy::positive)
            .toList();
        BigDecimal allArea = observations.stream()
            .map(BuildingObservation::grossFloorArea)
            .filter(BuiltEnvironmentStatisticsPolicy::positive)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal commercialArea = areas.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        int parking = commercial.stream()
            .mapToInt(value -> Math.max(0, value.parkingCount()))
            .sum();
        long aged = ages.stream()
            .filter(value -> value.compareTo(
                BigDecimal.valueOf(AGED_BUILDING_YEARS)
            ) >= 0)
            .count();
        return new BuiltEnvironmentStatistics(
            observations.size(),
            commercial.size(),
            average(ages),
            ratio(aged, ages.size()),
            average(areas),
            parking,
            divide(BigDecimal.valueOf(parking), commercial.size()),
            commercialArea,
            allArea.signum() == 0
                ? null
                : commercialArea.multiply(HUNDRED)
                    .divide(allArea, 4, RoundingMode.HALF_UP),
            observations.size()
        );
    }

    private static BigDecimal age(LocalDate approvalDate, LocalDate snapshotDate) {
        if (approvalDate == null || approvalDate.isAfter(snapshotDate)) {
            return null;
        }
        long days = ChronoUnit.DAYS.between(approvalDate, snapshotDate);
        return BigDecimal.valueOf(days)
            .divide(BigDecimal.valueOf(365.2425), 4, RoundingMode.HALF_UP);
    }

    private static BigDecimal average(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return null;
        }
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
    }

    private static BigDecimal ratio(long count, long total) {
        if (total == 0) {
            return null;
        }
        return BigDecimal.valueOf(count).multiply(HUNDRED)
            .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);
    }

    private static BigDecimal divide(BigDecimal value, int divisor) {
        return divisor == 0
            ? null
            : value.divide(BigDecimal.valueOf(divisor), 4, RoundingMode.HALF_UP);
    }

    private static boolean positive(BigDecimal value) {
        return value != null && value.signum() > 0;
    }
}
