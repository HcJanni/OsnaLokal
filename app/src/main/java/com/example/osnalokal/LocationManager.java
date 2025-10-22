package com.example.osnalokal;

import android.Manifest;
import android.content.Context;import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull; // Wichtig für @NonNull
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest; // Korrekter Import
import com.google.android.gms.location.LocationResult;  // Korrekter Import
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;        // Korrekter Import

public class LocationManager {

    private static LocationManager instance;
    private final FusedLocationProviderClient fusedLocationClient;
    private Location lastKnownLocation;
    private final LocationCallback locationCallback;

    private LocationManager(Context context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context.getApplicationContext());

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) { // Korrekte Klasse hier
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        lastKnownLocation = location;
                        Log.d("LocationManager", "Neuer Standort im Hintergrund: " + location.getLatitude());
                    }
                }
            }
        };
    }

    public static synchronized LocationManager getInstance(Context context) {
        if (instance == null) {
            instance = new LocationManager(context);
        }
        return instance;
    }

    public void startLocationUpdates(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w("LocationManager", "Keine Berechtigung für Standort-Updates.");
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 30000) // Korrekte Klasse hier
                .setMinUpdateIntervalMillis(15000)
                .build();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        Log.d("LocationManager", "Hintergrund-Standort-Updates gestartet.");
    }

    public void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        Log.d("LocationManager", "Hintergrund-Standort-Updates gestoppt.");
    }

    public Location getLastKnownLocation() {
        return lastKnownLocation;
    }
}
