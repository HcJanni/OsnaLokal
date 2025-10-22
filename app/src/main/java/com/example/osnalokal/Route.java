package com.example.osnalokal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Route {

    // --- ANGEPASSTE FELDER FÜR DAS NEUE KONZEPT ---
    private final int id;
    private final String name; // 'title' wurde zu 'name' für Konsistenz
    private final String description;
    private final String category;
    private final int imageResource;
    private final List<Integer> locationIds; // <-- Das ist das entscheidende neue Feld!
    private final int durationInMinutes; // Dauer in Minuten (flexibler als Stunden)
    private final String budget;         // "günstig", "mittel", "teuer"
    private final Set<String> tags;      // Eine Liste von Tags wie "vegetarisch", "asiatisch", "barrierefrei"


    public Route(int id, String name, String description, String category, int imageResource, List<Integer> locationIds,
                 int durationInMinutes, String budget, Set<String> tags) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.imageResource = imageResource;
        this.locationIds = locationIds;
        this.durationInMinutes = durationInMinutes;
        this.budget = budget;
        this.tags = tags;
    }

    public Route(int id, String name, String description, String category, int imageResource, List<Integer> locationIds,
                 int durationInMinutes, String budget) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.imageResource = imageResource;
        this.locationIds = locationIds;
        this.durationInMinutes = durationInMinutes;
        this.budget = budget;
        this.tags = new HashSet<>();
    }

    // --- ANGEPASSTE GETTER ---
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
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

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public String getBudget() {
        return budget;
    }

    public Set<String> getTags() {
        return tags;
    }
}
