package com.example.osnalokal;public class Route {

    private final String title;
    private final String description;
    private final String distance;
    private final int imageResource; // Wir verwenden erstmal lokale Bilder aus 'drawable'

    public Route(String title, String description, String distance, int imageResource) {
        this.title = title;
        this.description = description;
        this.distance = distance;
        this.imageResource = imageResource;
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
}
