// Globale Variablen
let map;
let userMarker = null;
let currentRouteLayer = null;
let currentDrawnMarkers = [];

// --- Pin-Icons ---
const defaultPin = L.icon({
    iconUrl: 'file:///android_res/drawable/mappin.png',
    iconSize:     [38, 38],
    iconAnchor:   [19, 38]
});
const greyPin = L.icon({
    iconUrl: 'file:///android_res/drawable/mappin_grey.png',
    iconSize:     [38, 38],
    iconAnchor:   [19, 38]
});
const userPin = L.icon({
    iconUrl: 'file:///android_res/drawable/directedlocationmarker.png',
    iconSize:     [38, 38],
    iconAnchor:   [19, 19]
});


function initializeMap() {
    console.log("Initialisiere die Karte...");
    map = L.map('map', {
        zoomControl: false
    });
    map.setView([52.2790, 8.0425], 15);

    L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
        maxZoom: 19,
        attribution: '© OpenStreetMap contributors, © CARTO'
    }).addTo(map);

    console.log("Karte initialisiert. Melde an Android, dass wir bereit sind.");
    if (window.Android && typeof window.Android.onMapReady === 'function') {
        window.Android.onMapReady();
    } else {
        console.error("Android Interface 'onMapReady' nicht gefunden!");
    }
}

function clearAllMarkers() {
    if (!map) return;
    console.log(`Entferne ${currentDrawnMarkers.length} alte Marker.`);
    currentDrawnMarkers.forEach(marker => {
        map.removeLayer(marker);
    });
    currentDrawnMarkers = [];
}

// KORREKTUR: Dies ist jetzt die EINZIGE Funktion, die von Java aufgerufen wird.
// Sie hat keine internen, doppelten Aufrufe mehr.
function loadLocationsFromAppBase64(base64String, isActive) {
    if (!map) {
        console.error("loadLocationsFromAppBase64 aufgerufen, aber Karte ist nicht bereit.");
        return;
    }
    console.log(`Empfange ${isActive ? 'aktive' : 'inaktive'} Locations via Base64.`);

    try {
        // 1. Dekodiere den Base64-String zurück zu einem normalen String
        const jsonString = atob(base64String);
        // 2. Parse den JSON-String in ein JavaScript-Array
        const locations = JSON.parse(jsonString);

        console.log(`Zeichne ${locations.length} Marker (Aktiv: ${isActive})`);
        const pinIcon = isActive ? defaultPin : greyPin;

        // 3. Iteriere über das Array und zeichne die Marker
        locations.forEach(location => {
            if (location.breitengrad == null || location.laengengrad == null) {
                console.warn("Ungültige Koordinaten für Location-ID:", location.id);
                return; // Überspringe diesen Marker
            }

            const marker = L.marker([location.breitengrad, location.laengengrad], { icon: pinIcon })
                .addTo(map)
                .on('click', () => {
                    if (window.Android && typeof window.Android.onMarkerClick === 'function') {
                       window.Android.onMarkerClick(location.id);
                    }
                });
            currentDrawnMarkers.push(marker);
        });

    } catch (e) {
        console.error("FATAL: Fehler beim Dekodieren/Parsen der Base64-Location-Daten:", e);
    }
}

// --- Restliche Funktionen ---
function drawRouteFromEncodedPath(encodedPath) {
    if (!map) return;
    if (currentRouteLayer) {
        map.removeLayer(currentRouteLayer);
    }
    const decodedCoords = decodeGooglePolyline(encodedPath);
    if (!decodedCoords || decodedCoords.length === 0) return;
    currentRouteLayer = L.polyline(decodedCoords, { color: '#E53935', weight: 5, opacity: 0.9 }).addTo(map);
    map.fitBounds(currentRouteLayer.getBounds().pad(0.1));
}

function updateUserLocationFromApp(lat, lng) {
    if (!map) return;
    const userLatLng = [lat, lng];
    if (userMarker) {
        userMarker.setLatLng(userLatLng);
    } else {
        userMarker = L.marker(userLatLng, { icon: userPin }).addTo(map);
    }
}

function updateUserHeading(heading) {
    if (userMarker && userMarker.setRotationAngle) {
        userMarker.setRotationAngle(heading);
    }
}

function centerOnUserLocation() {
    if (userMarker) {
        map.flyTo(userMarker.getLatLng(), 17);
    }
}

function decodeGooglePolyline(encoded) {
    let points = [], index = 0, len = encoded.length, lat = 0, lng = 0;
    while (index < len) {
        let b, shift = 0, result = 0;
        do { b = encoded.charCodeAt(index++) - 63; result |= (b & 0x1f) << shift; shift += 5; } while (b >= 0x20);
        const dlat = ((result & 1) ? ~(result >> 1) : (result >> 1)); lat += dlat;
        shift = 0; result = 0;
        do { b = encoded.charCodeAt(index++) - 63; result |= (b & 0x1f) << shift; shift += 5; } while (b >= 0x20);
        const dlng = ((result & 1) ? ~(result >> 1) : (result >> 1)); lng += dlng;
        points.push([lat / 1e5, lng / 1e5]);
    }
    return points;
}

initializeMap();
