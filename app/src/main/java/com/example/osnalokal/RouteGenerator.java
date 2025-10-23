package com.example.osnalokal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RouteGenerator {

    private final String googleApiKey;
    private static final int ABSOLUTE_MAX_SUGGESTIONS = 5;

    public RouteGenerator(String apiKey) {
        this.googleApiKey = apiKey;
    }

    // ===== KORREKTUR 1: DIES IST DIE ÖFFENTLICHE HAUPTMETHODE =====
    // Sie wird aus der MapActivity aufgerufen und gibt Routen zurück.
    public List<Route> generateRoutesFromFilter(FilterCriteria criteria) {
        // Ruft die private Methode auf, die die eigentliche Filterung vornimmt.
        List<Location> potentialLocations = findMatchingLocations(criteria);
        // Gibt das Ergebnis der Routenerstellung zurück.
        return createIntelligentSuggestions(potentialLocations, criteria);
    }

    // ===== KORREKTUR 2: DIES IST DIE ZWEITE, STATISCHE WERKZEUG-METHODE =====
    // Sie wird für den "Nur Orte anzeigen"-Modus gebraucht. Sie ist völlig getrennt.
    public static List<Location> findMatchingLocations(FilterCriteria criteria) {
        List<Location> allLocations = DataManager.getInstance().getAllLocations();
        return allLocations.stream()
                .filter(loc -> {
                    // Hauptkategorie-Prüfung
                    boolean categoryMatch = criteria.categories.contains(loc.getArt());
                    if (!categoryMatch) return false;

                    // Spezifische Filter für Restaurants
                    if (loc.getArt().equalsIgnoreCase("Restaurant") && !criteria.restaurantTags.isEmpty()) {

                        // 1. Prüfe auf Tags wie "vegetarisch" und "vegan"
                        if (criteria.restaurantTags.contains("vegetarisch") && !loc.isVegetarisch()) {
                            return false;
                        }
                        if (criteria.restaurantTags.contains("vegan") && !loc.isVegan()) {
                            return false;
                        }

                        // 2. Prüfe auf die Essensart (Asiatisch, Italienisch, etc.)
                        String foodTypeTag = null;
                        if (criteria.restaurantTags.contains("asiatisch")) foodTypeTag = "Asiatisch";
                        else if (criteria.restaurantTags.contains("italienisch")) foodTypeTag = "Italienisch";
                        else if (criteria.restaurantTags.contains("deutsch")) foodTypeTag = "Deutsch";
                        else if (criteria.restaurantTags.contains("andere")) foodTypeTag = "Andere";

                        if (foodTypeTag != null) {
                            if (loc.getEssensart() == null || !foodTypeTag.equalsIgnoreCase(loc.getEssensart())) {
                                return false;
                            }
                        }
                    }

                    // Budget-Filter
                    if (criteria.budget != null && !criteria.budget.isEmpty()) {
                        int budgetValue = 0;
                        if (criteria.budget.equals("günstig")) budgetValue = 1;
                        if (criteria.budget.equals("mittel")) budgetValue = 2;
                        if (criteria.budget.equals("teuer")) budgetValue = 3;

                        if (budgetValue == 1 && loc.getPreisspanne() >= 10) return false;
                        if (budgetValue == 2 && (loc.getPreisspanne() < 10 || loc.getPreisspanne() >= 20)) return false;
                        if (budgetValue == 3 && loc.getPreisspanne() < 20) return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    // ===== Der Rest der Klasse ist PRIVATE und wird intern verwendet. Keine Änderungen hier nötig. =====

    private List<Route> createIntelligentSuggestions(List<Location> potentialLocations, FilterCriteria criteria) {
        List<Route> suggestions = new ArrayList<>();

        int numberOfSuggestions = 3;
        if (criteria.maxDurationHours != null && criteria.maxDurationHours >= 4) {
            numberOfSuggestions = ABSOLUTE_MAX_SUGGESTIONS;
        }

        List<Location> topRestaurants = potentialLocations.stream()
                .filter(loc -> loc.getArt().equalsIgnoreCase("Restaurant"))
                .sorted(Comparator.comparing(Location::getBewertungen).reversed())
                .limit(numberOfSuggestions)
                .collect(Collectors.toList());

        for (int i = 0; i < numberOfSuggestions; i++) {
            List<Location> baseLocationsForThisSuggestion = new ArrayList<>();

            if (criteria.containsRestaurantCategory()) {
                if (i < topRestaurants.size()) {
                    baseLocationsForThisSuggestion.add(topRestaurants.get(i));
                } else {
                    break;
                }
            }

            for (String category : criteria.categories) {
                if (category.equalsIgnoreCase("Restaurant")) continue;
                potentialLocations.stream()
                        .filter(loc -> loc.getArt().equalsIgnoreCase(category))
                        .max(Comparator.comparing(Location::getBewertungen))
                        .ifPresent(baseLocationsForThisSuggestion::add);
            }

            if(baseLocationsForThisSuggestion.stream().map(Location::getArt).distinct().count() < criteria.categories.size()){
                continue;
            }

            int numberOfOptionalStops = 0;
            if (criteria.maxDurationHours != null) {
                numberOfOptionalStops = (int) Math.floor((criteria.maxDurationHours * 60 - 120) / 90.0);
            }
            numberOfOptionalStops += i;
            if (numberOfOptionalStops < 0) numberOfOptionalStops = 0;

            List<Location> finalRouteLocations = new ArrayList<>(baseLocationsForThisSuggestion);

            potentialLocations.stream()
                    .filter(loc -> !finalRouteLocations.contains(loc))
                    .filter(loc -> !loc.getArt().equalsIgnoreCase("Restaurant"))
                    .sorted(Comparator.comparing(Location::getBewertungen).reversed())
                    .limit(numberOfOptionalStops)
                    .forEach(finalRouteLocations::add);

            if(finalRouteLocations.size() > 8) continue;

            List<Location> sortedRoute = sortByProximity(finalRouteLocations);
            Route generatedRoute = createRouteFromLocations(sortedRoute, i, criteria);

            if (generatedRoute != null && isDurationAcceptable(generatedRoute.getDurationInMinutes(), criteria)) {
                suggestions.add(generatedRoute);
            }
        }
        return suggestions;
    }

    private List<Location> sortByProximity(List<Location> locations) {
        if (locations.size() < 2) return locations;
        ArrayList<Location> unsorted = new ArrayList<>(locations);
        List<Location> sorted = new ArrayList<>();
        Location current = unsorted.remove(0);
        sorted.add(current);

        while (!unsorted.isEmpty()) {
            Location finalCurrent = current;
            Location closest = Collections.min(unsorted, Comparator.comparingDouble(loc -> distance(finalCurrent, loc)));
            sorted.add(closest);
            unsorted.remove(closest);
            current = closest;
        }
        return sorted;
    }

    private boolean isDurationAcceptable(int routeDuration, FilterCriteria criteria) {
        if (criteria.minDurationHours == null && criteria.maxDurationHours == null) return true;
        int minDurationMinutes = criteria.minDurationHours != null ? criteria.minDurationHours * 60 : 0;
        int maxDurationMinutes = criteria.maxDurationHours != null ? criteria.maxDurationHours * 60 : Integer.MAX_VALUE;
        return routeDuration >= minDurationMinutes && routeDuration <= maxDurationMinutes;
    }

    private Route createRouteFromLocations(List<Location> locations, int suggestionIndex, FilterCriteria criteria) {
        if (locations.isEmpty()) return null;
        List<Integer> locationIds = locations.stream().map(Location::getId).collect(Collectors.toList());
        int estimatedDuration = (locations.size() - 1) * 15 + locations.size() * 30;
        String name = "Vorschlag " + (suggestionIndex + 1);

        if (locations.size() <= criteria.categories.size()) name += " (Kurz)";
        else name += " (Ausführlich)";

        return new Route(9000 + suggestionIndex, name, "Dynamisch für dich erstellt.",
                "Mix", R.drawable.rec_tours_testimg, locationIds, estimatedDuration);
    }

    private double distance(Location a, Location b) {
        double latDist = Math.pow(a.getBreitengrad() - b.getBreitengrad(), 2);
        double lonDist = Math.pow(a.getLaengengrad() - b.getLaengengrad(), 2);
        return Math.sqrt(latDist + lonDist);
    }
}
