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

const userPin = L.icon({
    iconUrl: 'file:///android_res/drawable/logopin.png',

    iconSize:     [38, 38],
    iconAnchor:   [19, 38],
    popupAnchor:  [0, -38]
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


function startGeolocation() {
    console.log("Starte Standortüberwachung...");

    if ('geolocation' in navigator) {
        // Definiere die Optionen für die Standortabfrage
        const options = {
            enableHighAccuracy: true, // Fordere eine hohe Genauigkeit an, wenn möglich
            timeout: 1000,            // Gib nach 8 Sekunden auf (verhindert endloses Warten)
            maximumAge: 0             // Erzwinge eine frische Positionsabfrage
        };

        // 1. Hole zuerst eine schnelle, einmalige Position
        navigator.geolocation.getCurrentPosition(
            (pos) => { // Erfolgs-Callback
                console.log("Erste schnelle Position gefunden!");
                updateLocation(pos);
            },
            (err) => { // Fehler-Callback
                console.warn("Fehler bei der ersten schnellen Positionsabfrage.", err);
                // Wenn die schnelle Abfrage fehlschlägt, ist es nicht schlimm,
                // da die kontinuierliche Überwachung trotzdem läuft.
            },
            options // Übergib die Optionen
        );

        // 2. Starte DANACH die kontinuierliche Überwachung
        navigator.geolocation.watchPosition(updateLocation, handleLocationError, options);

    } else {
        console.log("Geolocation wird von diesem Browser nicht unterstützt.");
    }
}

// Wird aufgerufen, wenn eine neue Position erfolgreich ermittelt wurde
function updateLocation(pos) {
    const lat = pos.coords.latitude;
    const lng = pos.coords.longitude;
    console.log(`Neue Position: ${lat}, ${lng}`);

    // Wenn schon ein Marker existiert, wird nur seine Position aktualisiert
    if (userMarker) {
        userMarker.setLatLng([lat, lng]);
    } else {
        // Erstelle den Marker nur, wenn er noch nicht existiert
        userMarker = L.marker([lat, lng], {icon: userPin}).addTo(map);
        userMarker.bindPopup("<b>Dein Standort</b>");
    }

    // Beim allerersten erfolgreichen Standort-Update: Fliege elegant zur Position des Nutzers.
    if (!hasZoomedToUser) {
        map.flyTo([lat, lng], 17); // Zoomt nah an die Position
        hasZoomedToUser = true;
    }
}

// Wird aufgerufen, wenn die Standort-Ermittlung fehlschlägt
function handleLocationError(err) {
    if (err.code === 1) { // 1 = PERMISSION_DENIED
        console.warn("Nutzer hat die Standortfreigabe verweigert.");
    } else if (err.code === 2) { // 2 = POSITION_UNAVAILABLE
        console.warn("Standortinformationen sind nicht verfügbar.");
    } else if (err.code === 3) { // 3 = TIMEOUT
        console.warn("Timeout bei der Standortabfrage.");
    } else {
        console.error(`Unbekannter Fehler bei der Standort-Ermittlung: ${err.message}`);
    }
}

// --- Initialisierung ---
initializeMap();
