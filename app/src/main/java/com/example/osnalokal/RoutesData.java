package com.example.osnalokal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RoutesData {

    /**
     * Gibt eine hardcodierte Liste aller vordefinierten Routen zurück.
     * @return Eine Liste von Route-Objekten.
     */
    public static List<Route> getAllRoutes() {
        List<Route> routes = new ArrayList<>();

        // Beispiel-Route 1: Asiatische Genüsse
        // Verwendet die IDs von "Buddha Bowl" (11) und "Viet Nam Wok" (12)
        routes.add(new Route(
                1001,
                "Asiatische Genüsse",
                "Entdecke die besten asiatischen Restaurants der Stadt.",
                R.drawable.rec_tours_testimg, // Platzhalter-Bild
                Arrays.asList(11, 12, 18) // IDs: Buddha Bowl, Viet Nam Wok, Pans Kitchen
        ));

        // Beispiel-Route 2: Eine Kneipen-Tour
        // Verwendet die IDs von "Grüner Jäger" (52), "Unikeller" (55) und "Trash" (54)
        routes.add(new Route(
                1002,
                "Osnabrücker Kneipen-Runde",
                "Eine klassische Tour durch die beliebtesten Kneipen der Altstadt.",
                R.drawable.rec_tours_testimg, // Platzhalter-Bild
                Arrays.asList(52, 55, 54)
        ));

        // Beispiel-Route 3: Kultur-Spaziergang
        // Verwendet die IDs von "Museumsquartier" (31), "Dom St. Peter" (42) und "Historische Altstadt" (45)
        routes.add(new Route(
                1003,
                "Kultur-Spaziergang",
                "Entdecke die kulturellen Highlights im Herzen Osnabrücks.",
                R.drawable.rec_tours_testimg, // Platzhalter-Bild
                Arrays.asList(31, 42, 45)
        ));

        // Füge hier einfach weitere Routen nach demselben Muster hinzu...

        return routes;
    }
}
