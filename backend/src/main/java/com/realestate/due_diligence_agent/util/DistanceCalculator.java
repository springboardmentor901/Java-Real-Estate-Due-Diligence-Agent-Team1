package com.realestate.due_diligence_agent.util;

public final class DistanceCalculator {

    private static final double EARTH_RADIUS_MILES = 3958.8;

    private DistanceCalculator() {
    }

    public static double calculateMiles(
            double lat1,
            double lon1,
            double lat2,
            double lon2) {

        double lat1Radians = Math.toRadians(lat1);
        double lat2Radians = Math.toRadians(lat2);

        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Radians)
                * Math.cos(lat2Radians)
                * Math.sin(deltaLon / 2)
                * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_MILES * c;
    }
}