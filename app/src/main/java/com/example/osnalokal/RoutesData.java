package com.example.osnalokal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class RoutesData {

    /**
     * Gibt eine hardcodierte Liste aller vordefinierten Routen zurück.
     * @return Eine Liste von Route-Objekten.
     */
    public static List<Route> getAllRoutes() {
        List<Route> routes = new ArrayList<>();

        // --- Restaurant-Routen ---

        routes.add(new Route(
                1001,
                "Asiatische Genüsse",
                "Entdecke die besten asiatischen Restaurants der Stadt.",
                "Restaurant", // Kategorie
                R.drawable.rec_tours_testimg,
                Arrays.asList(11, 12, 18), // Location IDs
                150, // Dauer: 2,5 Stunden (150 Minuten)
                "mittel", // Budget
                new HashSet<>(Arrays.asList("asiatisch", "vegetarisch", "vegan")) // Tags
        ));

        // --- Bars / Kneipen Routen ---

        routes.add(new Route(
                1002,
                "Osnabrücker Kneipen-Runde",
                "Eine klassische Tour durch die beliebtesten Kneipen der Altstadt.",
                "Bar", // Kategorie
                R.drawable.rec_tours_testimg,
                Arrays.asList(52, 54), // Location IDs
                180, // Dauer: 3 Stunden
                "günstig" // Budget
        ));

        // --- Sehenswürdigkeitsrouten ---

        routes.add(new Route(
                1003,
                "Kultur-Spaziergang",
                "Entdecke die kulturellen Highlights im Herzen Osnabrücks.",
                "Sehenswürdigkeiten", // Kategorie
                R.drawable.rec_tours_testimg,
                Arrays.asList(31, 42, 45), // Location IDs
                120, // Dauer: 2 Stunden
                "günstig" // Budget
        ));

        // -- Aktivitätsrouten ---

        // --- Parkrouten ---

        return routes;
    }
}
