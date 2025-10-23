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

    // Leerer Konstruktor
    public FilterCriteria() {}
}
