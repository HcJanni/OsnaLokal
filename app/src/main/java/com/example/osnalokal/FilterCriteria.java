package com.example.osnalokal;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

// Diese Klasse bündelt alle Filterkriterien in einem einzigen, übertragbaren Objekt.
public class FilterCriteria implements Serializable {

    // Dauer in Stunden (Integer, kann null sein, wenn nicht gesetzt)
    public Integer minDurationHours;
    public Integer maxDurationHours;

    // Budget als String ("€", "€€", "€€€"), kann null sein
    public String budget;

    // Hauptkategorien als Set von Strings ("Aktivitäten", "Sehenswürdigkeiten", etc.)
    public Set<String> categories = new HashSet<>();

    // Restaurant-spezifische Tags als Set ("vegetarisch", "vegan", etc.)
    public Set<String> restaurantTags = new HashSet<>();

    public boolean isSingleCategoryMode() {
        // Angenommen, 'categories' ist eine Liste oder ein Set der ausgewählten Kategorien
        return categories != null && categories.size() == 1;
    }

    public boolean hasAnyCategorySelected() {
        // Gibt true zurück, wenn entweder eine Hauptkategorie
        // oder ein Restaurant-Tag (was implizit "Restaurant" als Kategorie meint) gewählt ist.
        return !categories.isEmpty() || !restaurantTags.isEmpty();
    }

    public boolean containsRestaurantCategory() {
        return categories.stream().anyMatch(cat -> cat.equalsIgnoreCase("Restaurant"));
    }

    // Leerer Konstruktor
    public FilterCriteria() {}
}
