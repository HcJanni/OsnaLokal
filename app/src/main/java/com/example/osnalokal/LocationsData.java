package com.example.osnalokal;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LocationsData {

    /**
     * Liest die 'locations.json' aus dem assets-Ordner und wandelt sie
     * in eine Liste von Location-Objekten um.
     * @param context Der App-Kontext, wird benötigt, um auf die Assets zuzugreifen.
     * @return Eine Liste von Location-Objekten.
     */
    public static List<Location> getAllLocations(Context context) {
        try {
            // 1. Öffne die JSON-Datei aus dem 'assets'-Ordner
            InputStream inputStream = context.getAssets().open("locations.json");
            InputStreamReader reader = new InputStreamReader(inputStream);

            // 2. Erstelle ein Gson-Objekt, um das JSON zu parsen
            Gson gson = new Gson();

            // 3. Definiere den Typ, in den das JSON umgewandelt werden soll (eine Liste von Locations)
            Type listType = new TypeToken<ArrayList<Location>>(){}.getType();

            // 4. Wandle das JSON in eine Java-Liste um und gib sie zurück
            List<Location> locations = gson.fromJson(reader, listType);
            reader.close(); // Wichtig: Reader schließen
            return locations != null ? locations : new ArrayList<>(); // Sicherheitscheck

        } catch (IOException e) {
            // Wenn die Datei nicht gefunden wird oder ein Lesefehler auftritt
            e.printStackTrace();
            // Gib eine leere Liste zurück, damit die App nicht abstürzt
            return new ArrayList<>();
        }
    }
}
