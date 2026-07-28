package com.capstone.ai_insite.metric.domain.policy;

import java.math.BigDecimal;

public class CompetitionRadiusPolicy {

    private static final double EARTH_RADIUS_METERS = 6_371_008.8;

    public double distanceMeters(
        BigDecimal fromLatitude,
        BigDecimal fromLongitude,
        BigDecimal toLatitude,
        BigDecimal toLongitude
    ) {
        requireCoordinate(fromLatitude, "fromLatitude");
        requireCoordinate(fromLongitude, "fromLongitude");
        requireCoordinate(toLatitude, "toLatitude");
        requireCoordinate(toLongitude, "toLongitude");

        double fromLat = Math.toRadians(fromLatitude.doubleValue());
        double toLat = Math.toRadians(toLatitude.doubleValue());
        double deltaLat = toLat - fromLat;
        double deltaLon = Math.toRadians(
            toLongitude.doubleValue() - fromLongitude.doubleValue()
        );
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
            + Math.cos(fromLat) * Math.cos(toLat)
            * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        return EARTH_RADIUS_METERS
            * 2
            * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public boolean isWithinMeters(
        BigDecimal fromLatitude,
        BigDecimal fromLongitude,
        BigDecimal toLatitude,
        BigDecimal toLongitude,
        int radiusMeters
    ) {
        if (radiusMeters <= 0) {
            throw new IllegalArgumentException("Radius must be positive.");
        }
        return distanceMeters(
            fromLatitude,
            fromLongitude,
            toLatitude,
            toLongitude
        ) <= radiusMeters;
    }

    private static void requireCoordinate(BigDecimal value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required.");
        }
    }
}
