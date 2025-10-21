package com.example.osnalokal;

public class NewsItem {

    private final String title;
    private final String distance;
    private final int imageResource;

    public NewsItem(String title, String distance, int imageResource) {
        this.title = title;
        this.distance = distance;
        this.imageResource = imageResource;
    }

    // Getter-Methoden, damit der Adapter die Daten auslesen kann
    public String getTitle() {
        return title;
    }

    public String getDistance() {
        return distance;
    }

    public int getImageResource() {
        return imageResource;
    }
}
