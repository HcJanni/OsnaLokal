package com.example.osnalokal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class RoutesData {

    /**
     * Gibt eine kuratierte, logische und vielfältige Liste von 15 vordefinierten Routen zurück.
     * Der isSustainable-Wert wird pro Route basierend auf den locations.json-Daten gesetzt.
     * @return Eine Liste von Route-Objekten.
     */
    public static List<Route> getAllRoutes() {
        List<Route> routes = new ArrayList<>();

        // REGEL: Eine Route ist nachhaltig, wenn ALLE Orte darin laut locations.json nachhaltig sind.

        // --- 1. KULTUR & GESCHICHTE ---
        routes.add(new Route(
                1001, "Historisches Herz Osnabrücks", "Vom Dom durch den Hexengang zur historischen Altstadt – die wichtigsten Wahrzeichen.",
                "Sehenswuerdigkeit", R.drawable.rec_tours_testimg, Arrays.asList(42, 41, 45), 75,
                new HashSet<>(), true // Alle Orte sind nachhaltig
        ));

        // --- 2. GENUSS & GRÜN (MIX) ---
        routes.add(new Route(
                1002, "Pizza & Park", "Genieße eine exzellente Pizza bei Da Michele und entspanne danach im schönen Schlossgarten.",
                "Mix", R.drawable.rec_tours_testimg, Arrays.asList(15, 21), 120,
                new HashSet<>(), true // Beide Orte sind nachhaltig
        ));

        // --- 3. ENTDECKERTOUR FÜR EINEN TAG (MIX) ---
        routes.add(new Route(
                1003, "Ein perfekter Tag in Osnabrück", "Dom, Altstadt, ein leckeres Essen bei L'Osteria und zum Abschluss Entspannung im Schlossgarten.",
                "Mix", R.drawable.rec_tours_testimg, Arrays.asList(42, 45, 13, 21), 240,
                new HashSet<>(), true // Alle Orte sind nachhaltig
        ));

        // --- 4. VEGANE SCHLEMMERTOUR (RESTAURANT) ---
        routes.add(new Route(
                1004, "Vegan durch den Tag", "Vom herzhaften Burger bei Peter Pane zu einer gesunden Bowl im Buddha Bowl – alles rein pflanzlich.",
                "Restaurant", R.drawable.rec_tours_testimg, Arrays.asList(17, 11), 100,
                new HashSet<>(), true // Beide Orte sind nachhaltig
        ));

        // --- 5. AKTIV & SPANNEND (AKTIVITAETEN) ---
        routes.add(new Route(
                1005, "Film & Flucht", "Erst ein besonderer Film im Cinema Arthouse, danach stellt ihr euren Verstand bei Mindhunters auf die Probe.",
                "Aktivitaeten", R.drawable.rec_tours_testimg, Arrays.asList(33, 34), 210,
                new HashSet<>(), false // Beide Orte sind nicht nachhaltig
        ));

        // --- 6. ARCHITEKTUR-TOUR (SEHENSWUERDIGKEIT) ---
        routes.add(new Route(
                1006, "Türme und Mühlen", "Entdecke Osnabrücks alte Wachtürme auf diesem kurzen Spaziergang zum Plümersturm und zur Pernickelmühle.",
                "Sehenswuerdigkeit", R.drawable.rec_tours_testimg, Arrays.asList(43, 44), 45,
                new HashSet<>(), true // Beide Orte sind nachhaltig
        ));

        // --- 7. ASIATISCHE VIELFALT (RESTAURANT) ---
        routes.add(new Route(
                1007, "Asiatische Geschmackswelt", "Erlebe die Aromen Vietnams im Viet Nam Wok und genieße danach eine kreative Bowl im Buddha Bowl.",
                "Restaurant", R.drawable.rec_tours_testimg, Arrays.asList(12, 11), 90,
                new HashSet<>(), false // Viet Nam Wok ist nicht nachhaltig
        ));

        // --- 8. GEMÜTLICHER ABEND (MIX) ---
        routes.add(new Route(
                1008, "Spielen & Genießen", "Ein Abend voller Spaß im Spielcafé Osnabrett, perfekt abgerundet mit einem Drink in der Altstadt.",
                "Mix", R.drawable.rec_tours_testimg, Arrays.asList(51, 45), 150,
                new HashSet<>(), true // Beide Orte sind nachhaltig
        ));

        // --- 9. KULTURNACHMITTAG (MIX) ---
        routes.add(new Route(
                1009, "Kunst, Kultur & Kaffee", "Besuche das Museumsquartier und den Dom und lass die Eindrücke bei einem Kaffee in der Altstadt sacken.",
                "Mix", R.drawable.rec_tours_testimg, Arrays.asList(31, 42, 45), 180,
                new HashSet<>(), false // Museumsquartier ist nicht nachhaltig
        ));

        // --- 10. GRÜNE OASE (PARK & SEHENSWUERDIGKEIT) ---
        routes.add(new Route(
                1010, "Botanische Entspannung", "Entfliehe dem Alltagsstress im wunderschönen Botanischen Garten und dem angrenzenden Schlossgarten.",
                "Mix", R.drawable.rec_tours_testimg, Arrays.asList(47, 21), 150,
                new HashSet<>(), true // Beide Orte sind nachhaltig
        ));

        // --- 11. ITALIENISCHES FLAIR (RESTAURANT) ---
        routes.add(new Route(
                1011, "La Dolce Vita", "Genieße ein Stück Italien mit authentischer Pizza bei Da Michele und einem Eis bei La Pizza Sion.",
                "Restaurant", R.drawable.rec_tours_testimg, Arrays.asList(15, 19), 110,
                new HashSet<>(), true // Beide Orte sind nachhaltig
        ));

        // --- 12. NACHHALTIGER STADTBUMMEL (MIX) ---
        routes.add(new Route(
                1012, "Nachhaltig & Lecker", "Genieße eine orientalische Spezialität bei Al Sakr und entdecke danach die einzigartigen Cigköfte bei HasAntep.",
                "Restaurant", R.drawable.rec_tours_testimg, Arrays.asList(14, 16), 90,
                new HashSet<>(), true // Beide Orte sind nachhaltig
        ));

        // --- 13. STUDENTEN-ABEND (BAR) ---
        routes.add(new Route(
                1013, "Die Unikeller-Runde", "Ein Bier im Grünen Jaeger, eine Runde zocken im Countdown und der Absacker im legendären Unikeller.",
                "Bar", R.drawable.rec_tours_testimg, Arrays.asList(52, 53, 54), 180,
                new HashSet<>(), false // Grüner Jaeger & Countdown sind nicht nachhaltig
        ));

        // --- 14. ALTERNATIVE KNEIPENTOUR (BAR) ---
        routes.add(new Route(
                1014, "Von Trash bis Chic", "Eine Tour der Kontraste: Beginne im alternativen Trash und ende im geselligen Spielcafé Osnabrett.",
                "Bar", R.drawable.rec_tours_testimg, Arrays.asList(55, 51), 120,
                new HashSet<>(), true // Beide Orte sind nachhaltig (Annahme, da Trash in JSON fehlt, aber als Beispiel hier)
        ));

        // --- 15. QUICK-LUNCH (RESTAURANT) ---
        routes.add(new Route(
                1015, "Schnell & Gut", "Keine Zeit? Hol dir eine herausragende Cigköfte bei HasAntep oder eine leckere Bowl bei Buddha Bowl.",
                "Restaurant", R.drawable.rec_tours_testimg, Arrays.asList(16, 11), 60,
                new HashSet<>(), true // Beide Orte sind nachhaltig
        ));

        return routes;
    }
}
