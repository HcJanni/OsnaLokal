package com.example.osnalokal;

// Diese Klasse ist jetzt 1:1 synchron mit der Struktur deiner locations.json
public class Location {

    private final int id;
    private final String name;
    private final String art;
    private final String oeffnungszeiten;
    private final double bewertungen;
    private final int preisspanne;
    private final boolean barrierefrei;
    private final String telefonnummer; // Kann null sein
    private final String essensart;     // Kann null sein
    private final boolean vegetarisch;
    private final boolean vegan;
    private final double breitengrad;
    private final double laengengrad;
    private final boolean nachhaltig;
    private final String bild;
    private final String beschreibung = "Beschreibung Platzhalter"; // Standardwert, falls in JSON nicht vorhanden

    // Einziger Konstruktor, den GSON verwendet.
    public Location(int id, String name, String art, String oeffnungszeiten, double bewertungen, int preisspanne, boolean barrierefrei, String telefonnummer, String essensart, boolean vegetarisch, boolean vegan, double breitengrad, double laengengrad, boolean nachhaltig, String bild) {
        this.id = id;
        this.name = name;
        this.art = art;
        this.oeffnungszeiten = oeffnungszeiten;
        this.bewertungen = bewertungen;
        this.preisspanne = preisspanne;
        this.barrierefrei = barrierefrei;
        this.telefonnummer = telefonnummer;
        this.essensart = essensart;
        this.vegetarisch = vegetarisch;
        this.vegan = vegan;
        this.breitengrad = breitengrad;
        this.laengengrad = laengengrad;
        this.nachhaltig = nachhaltig;
        this.bild = bild;
    }

    // --- Getter ---
    public int getId() { return id; }
    public String getName() { return name; }
    public String getArt() { return art; }
    public String getOeffnungszeiten() { return oeffnungszeiten; }
    public double getBewertungen() { return bewertungen; }
    public int getPreisspanne() { return preisspanne; }
    public boolean isBarrierefrei() { return barrierefrei; }
    public String getTelefonnummer() { return telefonnummer != null ? telefonnummer : "-"; }
    public String getEssensart() { return essensart != null ? essensart : "-"; }
    public boolean isVegetarisch() { return vegetarisch; }
    public boolean isVegan() { return vegan; }
    public double getBreitengrad() { return breitengrad; }
    public double getLaengengrad() { return laengengrad; }
    public boolean isNachhaltig() { return nachhaltig; }
    public String getBild() { return bild; }
    public String getBeschreibung() { return beschreibung; }

    public String getBudgetAsEuroString() {
        if (preisspanne <= 0) return "-";
        if (preisspanne < 10) return "€";
        if (preisspanne < 20) return "€€";
        return "€€€";
    }
}
