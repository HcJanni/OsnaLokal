// Neue Datei: app/src/main/java/com/example/osnalokal/DataManager.java
package com.example.osnalokal;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DataManager {

    private static DataManager instance;

    private List<Location> allLocations;
    private List<Route> allRoutes;

    private DataManager() {
        // Initialisiere leere Listen, um Null-Fehler zu vermeiden
        allLocations = new ArrayList<>();
        allRoutes = new ArrayList<>();
    }

    public static synchronized DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    // Diese Methode wird EINMAL in der MainActivity aufgerufen
    public void loadAllData(Context context) {
        if (allLocations.isEmpty()) {
            try (InputStream inputStream = context.getAssets().open("locations.json");
                 Reader reader = new InputStreamReader(inputStream)) {
                Gson gson = new Gson();
                Type listType = new TypeToken<ArrayList<Location>>() {}.getType();
                allLocations = gson.fromJson(reader, listType);
                Log.d("DataManager", allLocations.size() + " Locations erfolgreich geladen.");
            } catch (Exception e) {
                Log.e("DataManager", "Fehler beim Laden von locations.json", e);
                allLocations = new ArrayList<>(); // Fehlerbehandlung
            }
        }

        if (allRoutes.isEmpty()) {
            // Angenommen, RoutesData lädt aus einer statischen Liste im Code
            allRoutes = RoutesData.getAllRoutes();
            Log.d("DataManager", allRoutes.size() + " Routen erfolgreich geladen.");
        }
    }

    public List<Location> getAllLocations() {
        return allLocations;
    }

    public List<Route> getAllRoutes() {
        return allRoutes;
    }
}
