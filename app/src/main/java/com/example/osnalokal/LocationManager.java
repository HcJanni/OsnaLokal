package com.example.osnalokal;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationManager {

    // 1. Definiere das einfache Listener-Interface.
    //    Es ist als "FunctionalInterface" markiert, perfekt für Lambdas.
    @FunctionalInterface
    public interface SimpleLocationListener {
        void onLocationUpdated(Location location);
    }

    private static LocationManager instance;
    private final FusedLocationProviderClient fusedLocationClient;
    private final LocationCallback locationCallback;

    // 2. Eine Variable, um den aktiven Listener zu speichern.
    private SimpleLocationListener activeListener;

    private LocationManager(Context context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context.getApplicationContext());

        // Der interne LocationCallback von Google.
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        // 3. Wenn ein neuer Standort ankommt, benachrichtige den aktiven Listener.
                        if (activeListener != null) {
                            activeListener.onLocationUpdated(location);
                        }
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

    /**
     * Startet die Standort-Updates und benachrichtigt den übergebenen Listener.
     * @param context Der App-Kontext, um die Berechtigungen zu prüfen.
     * @param listener Der Listener (kann eine Lambda-Funktion sein), der die Updates empfängt.
     */
    public void startLocationUpdates(Context context, SimpleLocationListener listener) {
        // 4. Speichere den übergebenen Listener als den aktiven Listener.
        this.activeListener = listener;

        // Prüfe, ob die Berechtigung erteilt wurde.
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w("LocationManager", "Keine Berechtigung für Standort-Updates.");
            return;
        }

        // Erstelle eine Anfrage für hochpräzise und regelmäßige Updates.
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000) // Update-Intervall: 5 Sekunden
                .setMinUpdateIntervalMillis(2000) // Minimales Intervall: 2 Sekunden
                .build();

        // Fordere die Updates an.
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        Log.d("LocationManager", "Standort-Updates gestartet.");
    }

    /**
     * Stoppt die Standort-Updates.
     */
    public void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        // 5. Entferne den Listener, da keine Updates mehr kommen.
        this.activeListener = null;
        Log.d("LocationManager", "Standort-Updates gestoppt.");
    }
}
