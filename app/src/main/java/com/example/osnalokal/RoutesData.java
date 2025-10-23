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
                new HashSet<>(Arrays.asList("asiatisch", "vegetarisch", "vegan")), // Tags
                true // sustainable
        ));

        // --- Bars / Kneipen Routen ---

        routes.add(new Route(
                1002,
                "Osnabrücker Kneipen-Runde",
                "Eine klassische Tour durch die beliebtesten Kneipen der Altstadt.",
                "Bar", // Kategorie
                R.drawable.rec_tours_testimg,
                Arrays.asList(52, 54), // Location IDs
                180
        ));

        // --- Sehenswürdigkeitsrouten ---

        routes.add(new Route(
                1003,
                "Kultur-Spaziergang",
                "Entdecke die kulturellen Highlights im Herzen Osnabrücks.",
                "Sehenswürdigkeiten", // Kategorie
                R.drawable.rec_tours_testimg,
                Arrays.asList(31, 42, 45), // Location IDs
                120
        ));

        routes.add(new Route(
                1003,
                "Eine nachhaltige Kulturreise",
                "Begib dich auf eine Entdeckungstour zu den kulturellen Schätzen und nachhaltigen Orten, die im Herzen Osnabrücks verborgen liegen.",
                "mix", // Kategorie
                R.drawable.rec_tours_testimg,
                Arrays.asList(21,11, 41, 17), // Location IDs
                120
        ));

        routes.add(new Route(
                1003,
                "Eine nicht nachhaltige Kulturreise",
                "Begib dich auf eine Entdeckungstour zu den kulturellen Schätzen und nicht nachhaltigen Orten, die im Herzen Osnabrücks verborgen liegen.",
                "mix", // Kategorie
                R.drawable.rec_tours_testimg,
                Arrays.asList(31,33, 34, 52), // Location IDs
                120
        ));



        // -- Aktivitätsrouten ---

        // --- Parkrouten ---

        return routes;
    }
}
