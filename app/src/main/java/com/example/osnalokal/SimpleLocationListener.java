package com.example.osnalokal;

import android.location.Location;

@FunctionalInterface
public interface SimpleLocationListener {
    void onLocationUpdated(Location location);
}