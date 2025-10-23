package com.example.osnalokal;

public class NewsItem {

    private final String title;
    private String imagePfad;
    private final String description;

    public NewsItem(String title, String description, String imagePfad) {
        this.title = title;
        this.description = description;
        this.imagePfad = imagePfad;
    }

    // Getter-Methoden, damit der Adapter die Daten auslesen kann
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImagePfad() { // Beachte den Namen "getImagePath"
        if (imagePfad == null || imagePfad.isEmpty()) {
            return "file://android_asset/Pictures/Sehenswürdigkeiten/55_Altstadt/55_2.jpg";
        }
        return imagePfad;
    }
}
