// --- Globale Variablen ---
let map;
let userMarker = null;let hasZoomedToUser = false;
let allLocations = []; // Hier speichern wir alle Orte aus der JSON-Datei
let locationMarkers = []; // NEU: Hier speichern wir alle Marker-Objekte

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

//Jetziger Standort Pin wird mit Logo angezeigt
//const userPin = L.icon({
//    iconUrl: 'file:///android_res/drawable/logopin.png',
//    iconSize:     [38, 38],
//    iconAnchor:   [19, 38],
//    popupAnchor:  [0, -38]
//});
//
//function updateLocation(pos) {
//    const lat = pos.coords.latitude;
//    const lng = pos.coords.longitude;
//    const accuracy = pos.coords.accuracy;
//    console.log(`Neue Position: ${lat}, ${lng} (Genauigkeit: ${accuracy}m)`);
//
//    if (userMarker) {
//        userMarker.setLatLng([lat, lng]);
//    } else {
//        // Verwende dein User-Icon
//        userMarker = L.marker([lat, lng], {icon: userPin}).addTo(map);
//    }

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

function resetAllMarkers() {
    locationMarkers.forEach(m => m.setIcon(defaultPin));
}

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

function drawRouteFromApp(startCoordsString, endCoordsString, apiKey) {
    console.log("Routenzeichnung von der App angefordert.");

    const startLatLon = startCoordsString.split(',');
    const endLatLon = endCoordsString.split(',');

    const startPoint = {
        name: "Start",
        lat: parseFloat(startLatLon[0]),
        lng: parseFloat(startLatLon[1])
    };

    const endPoint = {
        name: "Ziel",
        lat: parseFloat(endLatLon[0]),
        lng: parseFloat(endLatLon[1])
    };

    // Blende alle Location-Marker aus, um die Route hervorzuheben
    locationMarkers.forEach(m => map.removeLayer(m));

    // Rufe die Funktion auf, um die Route abzurufen und zu zeichnen
    fetchAndDrawGoogleRoute(startPoint, endPoint, apiKey);
}


// Passen Sie die fetchAndDrawGoogleRoute Funktion an, um den API-Key als Parameter zu erhalten
async function fetchAndDrawGoogleRoute(origin, destination, apiKey) {
    console.log(`Rufe Google Directions API ab für: ${origin.name} -> ${destination.name}`);

    // Der API-Schlüssel wird jetzt als Parameter übergeben
    if (!apiKey) {
        console.error("API Key fehlt!");
        alert("Routenplanung fehlgeschlagen: API-Schlüssel nicht konfiguriert.");
        return;
    }

    const apiUrl = `https://maps.googleapis.com/maps/api/directions/json?origin=${origin.lat},${origin.lng}&destination=${destination.lat},${destination.lng}&mode=walking&key=${apiKey}`;

    try {
        const response = await fetch(apiUrl);
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(`Google API Fehler: ${response.status} - ${errorData.error_message || response.statusText}`);
        }
        const data = await response.json();
        console.log("Google Directions Antwort:", data);

        if (data.status !== 'OK' || !data.routes || data.routes.length === 0) {
            console.error("Keine Route von Google gefunden:", data.status, data.error_message);
            alert(`Routenplanung fehlgeschlagen: ${data.error_message || data.status}`);
            return;
        }

        const route = data.routes[0];
        const encodedPolyline = route.overview_polyline.points;

        const decodedCoords = decodeGooglePolyline(encodedPolyline);

        drawRoutePolyline(decodedCoords);
        addStartEndMarkers(origin, destination); // Hinzugefügt für visuelles Feedback

    } catch (error) {
        console.error("Fehler beim Abrufen/Verarbeiten der Google Directions:", error);
        alert("Fehler bei der Routenplanung. Prüfe Internetverbindung und API Key.");
    }
}


// --- Funktion zum Zeichnen der Polyline (unverändert) ---
function drawRoutePolyline(coordinates) {
    // Alte Route entfernen
    if (currentRouteLayer) {
        map.removeLayer(currentRouteLayer);
    }
    if (!coordinates || coordinates.length === 0) return;
    console.log("Zeichne Google Route Polyline...");
    currentRouteLayer = L.polyline(coordinates, {
        color: '#4A89F3', // Google Blau
        weight: 6,
        opacity: 0.8
    }).addTo(map);

    // Zoom auf die gesamte Route
    map.fitBounds(currentRouteLayer.getBounds());
}

// NEUE Funktion, um Start- & End-Marker hinzuzufügen
function addStartEndMarkers(origin, destination) {
    // Alte Marker entfernen
    startEndMarkers.forEach(m => map.removeLayer(m));
    startEndMarkers = [];

    const startMarker = L.marker([origin.lat, origin.lng]).addTo(map).bindPopup("Start");
    const endMarker = L.marker([destination.lat, destination.lng]).addTo(map).bindPopup("Ziel");

    startEndMarkers.push(startMarker, endMarker);
}


// --- decodeGooglePolyline bleibt unverändert ---
function decodeGooglePolyline(encoded) {
    // Ihr Code hier... (keine Änderung nötig)
    let points = [];
    let index = 0, len = encoded.length;
    let lat = 0, lng = 0;

    while (index < len) {
        let b, shift = 0, result = 0;
        do {
            b = encoded.charCodeAt(index++) - 63;
            result |= (b & 0x1f) << shift;
            shift += 5;
        } while (b >= 0x20);

        const dlat = ((result & 1) ? ~(result >> 1) : (result >> 1));
        lat += dlat;

        shift = 0;
        result = 0;
        do {
            b = encoded.charCodeAt(index++) - 63;
            result |= (b & 0x1f) << shift;
            shift += 5;
        } while (b >= 0x20);

        const dlng = ((result & 1) ? ~(result >> 1) : (result >> 1));
        lng += dlng;
        points.push([lat / 1e5, lng / 1e5]);
    }
    return points;
}


// --- Initialisierung ---
initializeMap();