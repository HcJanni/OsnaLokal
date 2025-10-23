package com.example.osnalokal;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LocationsData {

    private static List<Location> allLocations = null;

    // Diese Methode MUSS einen Context haben, um auf Assets zugreifen zu können.
    public static List<Location> getAllLocations(Context context) {
        if (allLocations == null) {
            try (InputStream inputStream = context.getAssets().open("locations.json");
                 Reader reader = new InputStreamReader(inputStream)) {

                Gson gson = new Gson();
                Type listType = new TypeToken<ArrayList<Location>>() {}.getType();
                allLocations = gson.fromJson(reader, listType);

            } catch (Exception e) {
                e.printStackTrace();
                // Gib im Fehlerfall eine leere Liste zurück, um Abstürze zu vermeiden
                return new ArrayList<>();
            }
        }
        return allLocations;
    }
}
