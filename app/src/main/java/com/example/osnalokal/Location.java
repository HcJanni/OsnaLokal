package com.example.osnalokal;

public class Location {

    private final int id;
    private final String name;
    private final String art; // "Restaurant", "Bar", etc.
    private final double bewertungen;
    private final int preisspanne;
    private int budget;
    private final boolean barrierefrei;
    private final String telefonnummer;
    private final String essensart; // "Asiatisch", "Italienisch", etc.
    private final boolean vegetarisch;
    private final boolean vegan;
    private final double breitengrad;
    private final double laengengrad;
    private final String oeffnungszeiten;
    private final String beschreibung;// <-- 1. NEUES FELD HINZUGEFÜGT

    // Konstruktor, um neue Location-Objekte zu erstellen
    // 2. KONSTRUKTOR UM DAS NEUE FELD ERWEITERT
    public Location(int id, String name, String art, double bewertungen, int preisspanne, boolean barrierefrei, String telefonnummer, String essensart, boolean vegetarisch, boolean vegan, double breitengrad, double laengengrad, String oeffnungszeiten, String beschreibung) {
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
        this.oeffnungszeiten = oeffnungszeiten; // Zuweisung für das neue Feld
        this.beschreibung = beschreibung;
    }

    // Getter-Methoden, damit andere Teile der App auf die Daten zugreifen können
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
}
