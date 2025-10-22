// --- Globale Variablen ---
let map;
let userMarker = null;let hasZoomedToUser = false;
let allLocations = []; // Hier speichern wir alle Orte aus der JSON-Datei
let locationMarkers = []; // NEU: Hier speichern wir alle Marker-Objekte

// ==========================================================
// NEU: Definiere die beiden Pin-Icons
// ==========================================================
const defaultPin = L.icon({
    iconUrl: 'file:///android_res/drawable/mappin.png',
    iconSize:     [38, 38],
    iconAnchor:   [19, 38]
});

const selectedPin = L.icon({
    iconUrl: 'file:///android_res/drawable/mappin_selected.png',
    iconSize:     [38, 38],
    iconAnchor:   [19, 38]
});


function initializeMap() {
    console.log("Initialisiere die Karte...");

    map = L.map('map');
    map.setView([52.2790, 8.0425], 15); // Default View auf Osnabrück

    L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
        maxZoom: 19,
        attribution: '© OpenStreetMap contributors, © CARTO'
    }).addTo(map);

    startGeolocation();
}

function loadLocationsFromApp(jsonString) {
    console.log("Externe Orte werden geladen...");
    try {
        allLocations = JSON.parse(jsonString);
        addMarkersForLocations();
    } catch (e) {
        console.error("Fehler beim Parsen des JSON-Strings:", e);
    }
}

// ==========================================================
// NEU: Funktion zum Zurücksetzen aller Marker
// ==========================================================
function resetAllMarkers() {
    locationMarkers.forEach(m => m.setIcon(defaultPin));
}

// ==========================================================
// ANGEPASSTE FUNKTION: Fügt Marker hinzu und speichert sie
// ==========================================================
function addMarkersForLocations() {
    console.log(`Füge ${allLocations.length} Marker hinzu...`);
    if (!map || !allLocations) return;

    // Leere die alte Marker-Liste, falls die Funktion erneut aufgerufen wird
    locationMarkers.forEach(m => map.removeLayer(m));
    locationMarkers = [];

    allLocations.forEach(location => {
        if (location.breitengrad && location.laengengrad) {
            // Erstelle den Marker mit dem Standard-Pin
            const marker = L.marker([location.breitengrad, location.laengengrad], {icon: defaultPin}).addTo(map);

            // Speichere den Marker im Array
            locationMarkers.push(marker);

            // Füge den Klick-Listener hinzu
            marker.on('click', function() {
                // 1. Setze alle Marker auf den Standard-Pin zurück
                resetAllMarkers();

                // 2. Setze NUR den geklickten Marker auf den ausgewählten Pin
                marker.setIcon(selectedPin);

                map.panTo(marker.getLatLng());

                // 3. Rufe die Java-Methode auf, um das Bottom Sheet zu öffnen
                if (window.Android && typeof window.Android.onMarkerClick === 'function') {
                    window.Android.onMarkerClick(location.id);
                } else {
                    console.log("Android-Interface nicht gefunden.");
                }
            });
        }
    });
}

// --- Geolocation-Funktionen (bleiben unverändert) ---
// ... (startGeolocation, updateLocation, handleLocationError) ...

// --- Initialisierung ---
initializeMap();
