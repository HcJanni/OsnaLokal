package com.example.osnalokal;

import java.util.List;

public class Route {

    // --- ANGEPASSTE FELDER FÜR DAS NEUE KONZEPT ---
    private final int id;
    private final String name; // 'title' wurde zu 'name' für Konsistenz
    private final String description;
    private final int imageResource;
    private final List<Integer> locationIds; // <-- Das ist das entscheidende neue Feld!

    // --- ANGEPASSTER KONSTRUKTOR ---
    public Route(int id, String name, String description, int imageResource, List<Integer> locationIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageResource = imageResource;
        this.locationIds = locationIds;
    }

    // --- ANGEPASSTE GETTER ---
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResource() {
        return imageResource;
    }

    public List<Integer> getLocationIds() {
        return locationIds;
    }
}
