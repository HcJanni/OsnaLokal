// Erstelle diese Klasse oder als innere Klasse in MapActivity
package com.example.osnalokal;

import java.util.List;

public class MapRouteSuggestion {
    private final Route originalRoute;
    private final String locationsString;
    private final String durationString;
    private final String distanceString;

    public MapRouteSuggestion(Route originalRoute, String locationsString, String durationString, String distanceString) {
        this.originalRoute = originalRoute;
        this.locationsString = locationsString;
        this.durationString = durationString;
        this.distanceString = distanceString;
    }

    // Getter
    public Route getOriginalRoute() { return originalRoute; }
    public String getLocationsString() { return locationsString; }
    public String getDurationString() { return durationString; }
    public String getDistanceString() { return distanceString; }
}
