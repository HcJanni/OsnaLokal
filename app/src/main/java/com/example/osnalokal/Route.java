package com.example.osnalokal;

import java.io.Serializable; // <-- WICHTIG: Import hinzufügen
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Route implements Serializable {
    private final int id;
    private final String name;
    private final String description;
    private final String category;
    private final String imagePfad;
    private final List<Integer> locationIds;
    private final int durationInMinutes;
    private final Set<String> tags;
    private final boolean isSustainable;

    public Route(int id, String name, String description, String category, String imagePfad, List<Integer> locationIds,
                 int durationInMinutes, Set<String> tags, boolean isSustainable) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.imagePfad = imagePfad;
        this.locationIds = locationIds;
        this.durationInMinutes = durationInMinutes;
        this.tags = tags;
        this.isSustainable = isSustainable;
    }

    public Route(int id, String name, String description, String category, String imagePfad, List<Integer> locationIds,
                 int durationInMinutes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.imagePfad = imagePfad;
        this.locationIds = locationIds;
        this.durationInMinutes = durationInMinutes;
        this.tags = new HashSet<>();
        this.isSustainable = false;
    }

    // --- GETTER (unverändert) ---
    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getImagePfad() { return imagePfad; }
    public List<Integer> getLocationIds() { return locationIds; }
    public int getDurationInMinutes() { return durationInMinutes; }
    public Set<String> getTags() { return tags; }
    public boolean isSustainable() { return isSustainable; }
}
