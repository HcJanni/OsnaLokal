package com.example.osnalokal;public class Route {

    private final String title;
    private final String description;
    private final String distance;
    private final int imageResource; // Wir verwenden erstmal lokale Bilder aus 'drawable'
    private final String category;
    private final boolean isSuggested;

    public Route(String title, String description, String distance, int imageResource, String category, boolean isSuggested) {
        this.title = title;
        this.description = description;
        this.distance = distance;
        this.imageResource = imageResource;
        this.category = category;
        this.isSuggested = isSuggested;
    }

    // Getter-Methoden, damit der Adapter die Daten auslesen kann
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDistance() {
        return distance;
    }

    public int getImageResource() {
        return imageResource;
    }

    public String getCategory() {
        return category;
    }

    public boolean isSuggested() {
        return isSuggested;
    }
}
