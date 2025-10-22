package com.example.osnalokal;

public class NewsItem {

    private final String title;
    private final int imageResource;
    private final String description;

    public NewsItem(String title, String description, int imageResource) {
        this.title = title;
        this.description = description;
        this.imageResource = imageResource;
    }

    // Getter-Methoden, damit der Adapter die Daten auslesen kann
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResource() {
        return imageResource;
    }
}
