package com.civicpulse.util;

public class GeoUtil {
    
    private static final int EARTH_RADIUS_KM = 6371;

    /**
     * Calculate distance between two GPS coordinates using Haversine formula
     * @return distance in kilometers
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Check if two locations are within specified radius
     */
    public static boolean isWithinRadius(double lat1, double lon1, double lat2, double lon2, double radiusKm) {
        double distance = calculateDistance(lat1, lon1, lat2, lon2);
        return distance <= radiusKm;
    }

    /**
     * Calculate bounding box for geo queries
     * Returns array: [minLat, maxLat, minLng, maxLng]
     */
    public static double[] getBoundingBox(double lat, double lng, double radiusKm) {
        double latDelta = radiusKm / 111.0; // Approximate km per degree latitude
        double lngDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));
        
        return new double[] {
            lat - latDelta,  // minLat
            lat + latDelta,  // maxLat
            lng - lngDelta,  // minLng
            lng + lngDelta   // maxLng
        };
    }
}
