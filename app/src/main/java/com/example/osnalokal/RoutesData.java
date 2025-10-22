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

        // Route 1 bekommt die Kategorie "Kulinarik"
        routes.add(new Route(
                1001,
                "Asiatische Genüsse",
                "Entdecke die besten asiatischen Restaurants der Stadt.",
                "Kulinarik", // <-- KATEGORIE HINZUGEFÜGT
                R.drawable.rec_tours_testimg,
                Arrays.asList(11, 12, 18)
        ));

        // Route 2 bekommt die Kategorie "Nachtleben"
        routes.add(new Route(
                1002,
                "Osnabrücker Kneipen-Runde",
                "Eine klassische Tour durch die beliebtesten Kneipen der Altstadt.",
                "Nachtleben", // <-- KATEGORIE HINZUGEFÜGT
                R.drawable.rec_tours_testimg,
                Arrays.asList(52, 55, 54)
        ));

        // Route 3 bekommt die Kategorie "Kultur"
        routes.add(new Route(
                1003,
                "Kultur-Spaziergang",
                "Entdecke die kulturellen Highlights im Herzen Osnabrücks.",
                "Kultur", // <-- KATEGORIE HINZUGEFÜGT
                R.drawable.rec_tours_testimg,
                Arrays.asList(31, 42, 45)
        ));

        return routes;
    }
}
