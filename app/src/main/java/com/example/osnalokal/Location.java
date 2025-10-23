package com.example.osnalokal;
import com.google.gson.annotations.SerializedName;

public class Location {

    private final int id;
    private final String name;
    private final String art;
    private final double bewertungen;
    private final int preisspanne;
    private int budget;
    private final boolean barrierefrei;
    private final String telefonnummer;
    private final String essensart;
    private final boolean vegetarisch;
    private final boolean vegan;
    private final double breitengrad;
    private final double laengengrad;
    private final String oeffnungszeiten;
    private final String beschreibung;
    @SerializedName("bild")
    private String imagePfad;

    public Location(int id, String name, String art, double bewertungen, int preisspanne, boolean barrierefrei, String telefonnummer, String essensart, boolean vegetarisch, boolean vegan, double breitengrad, double laengengrad, String oeffnungszeiten, String beschreibung, String imagePfad) {
        this.id = id;
        this.name = name;
        this.art = art;
        this.bewertungen = bewertungen;
        this.preisspanne = preisspanne;
        this.barrierefrei = barrierefrei;
        this.telefonnummer = telefonnummer;
        this.essensart = essensart;
        this.vegetarisch = vegetarisch;
        this.vegan = vegan;
        this.breitengrad = breitengrad;
        this.laengengrad = laengengrad;
        this.oeffnungszeiten = oeffnungszeiten;
        this.beschreibung = beschreibung;
        this.imagePfad = imagePfad;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getArt() {
        return art;
    }

    public double getBewertungen() {
        return bewertungen;
    }

    public int getPreisspanne() {
        return preisspanne;
    }

    public String getBudgetAsEuroString() {
        if (preisspanne <= 0) {
            return "-"; // Falls kein Wert gesetzt ist
        } else if (preisspanne < 10) {
            return "€";
        } else if (preisspanne < 20) {
            return "€€";
        } else {
            return "€€€";
        }
    }

    public boolean isBarrierefrei() {
        return barrierefrei;
    }

    public String getTelefonnummer() {
        return telefonnummer;
    }

    public String getEssensart() {
        return essensart;
    }

    public boolean isVegetarisch() {
        return vegetarisch;
    }

    public boolean isVegan() {
        return vegan;
    }

    public double getBreitengrad() {
        return breitengrad;
    }

    public double getLaengengrad() {
        return laengengrad;
    }

    // 3. NEUER GETTER FÜR DIE ÖFFNUNGSZEITEN HINZUGEFÜGT
    public String getOeffnungszeiten() {
        return oeffnungszeiten;
    }

    public String getBeschreibung() {
        return beschreibung;
    }
    public String getImagePfad() {
        // Wichtig: Gib einen Fallback zurück, falls kein Bild da ist
        if (imagePfad == null || imagePfad.isEmpty()) {
            return "file://android_asset/Pictures/Sehenswürdigkeiten/55_Altstadt/55_3.jpg";
        }
        return imagePfad;
    }
}
