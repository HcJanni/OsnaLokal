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
    iconUrl: 'file:///android_res/drawable/locationmarker.png',

    iconSize:     [38, 38],
    iconAnchor:   [19, 19],
    popupAnchor:  [0, -19]
});


function initializeMap() {
    console.log("Initialisiere die Karte...");

    map = L.map('map');
    map.setView([52.2790, 8.0425], 15); // Default View auf Osnabrück

    L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
        maxZoom: 19,
        attribution: '© OpenStreetMap contributors, © CARTO'
    }).addTo(map);
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

function centerOnUserLocation() {
    if (userMarker) {
        map.flyTo(userMarker.getLatLng(), 17); // Fliege elegant zur Position
    } else {
        console.log("User-Position ist noch nicht bekannt.");
        // Optional: Hier könnte man eine Toast-Nachricht über das Android-Interface anzeigen
    }
}

function updateUserLocationFromApp(lat, lng) {
    console.log(`User-Position von App empfangen: ${lat}, ${lng}`);
    const userLatLng = [lat, lng];

    if (userMarker) {
        userMarker.setLatLng(userLatLng);
    } else {
        userMarker = L.marker(userLatLng, {icon: userPin}).addTo(map);
        userMarker.bindPopup("<b>Dein Standort</b>");
    }
}

// Passe die centerOnUserLocation-Funktion an. Der Fallback ist jetzt unwahrscheinlicher.
function centerOnUserLocation() {
    if (userMarker) {
        map.flyTo(userMarker.getLatLng(), 17);
    } else {
        console.log("User-Position ist noch nicht bekannt. Bitte warte einen Moment.");
    }
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
        const data = await response.json();
        console.log("Google Directions Antwort:", data);

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(`Google API Fehler: ${response.status} - ${errorData.error_message || response.statusText}`);
        }

        if (data.status !== 'OK' || !data.routes || data.routes.length === 0) {
            console.error("Keine Route von Google gefunden:", data.status, data.error_message);
            alert(`Routenplanung fehlgeschlagen: ${data.error_message || data.status}`);
            return;
        }

        const route = data.routes[0];
        const leg = route.legs[0];

        const distanceText = leg.distance.text; // z.B. "1,8 km"
        const durationText = leg.duration.text; // z.B. "23 Min."

        console.log(`Distanz: ${distanceText}, Dauer: ${durationText}`);
        alert(`Geschätzte Route:\nDistanz: ${distanceText}\nDauer: ${durationText}`);

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

function drawRouteFromEncodedPath(encodedPath) {
    // Die Leaflet-Bibliothek kann mit Google's Polyline-Format umgehen
    // (benötigt das leaflet-polyline-Plugin, das wir aber hier direkt einbinden)

    // Lade das Polyline-Utility von Leaflet
    if (typeof L.Polyline.fromEncoded === 'undefined') {
        // Wenn das Plugin nicht da ist, können wir nichts tun.
        // Für eine robustere Lösung müsste man das Plugin explizit einbinden.
        // Wir verwenden hier einen kleinen Hack, um es direkt zu nutzen.
        console.error("Leaflet Polyline Plugin nicht gefunden.");
        return;
    }

    try {
        // Dekodiere den Pfad und erstelle eine Polyline (die blaue Linie)
        var latlngs = L.Polyline.fromEncoded(encodedPath).getLatLngs();

        var polyline = L.polyline(latlngs, {
            color: '#03a9f4', // Helles Blau
            weight: 5,
            opacity: 0.7
        }).addTo(map);

        // Zoome die Karte so, dass die gesamte Route sichtbar ist
        map.fitBounds(polyline.getBounds().pad(0.1));

    } catch (e) {
        console.error("Fehler beim Zeichnen der Route: ", e);
    }
}

// Damit L.Polyline.fromEncoded funktioniert, müssen wir sicherstellen,
// dass das nötige Utility geladen ist. Leaflet selbst bringt es oft nicht mit.
// Hier ist eine Implementierung des Decoders, falls er fehlt.
(function () {
    var L = window.L;
    if (L && L.Polyline) {
        L.Polyline.fromEncoded = function (encoded, options) {
            var coords = decode(encoded, 5);
            var latlngs = new Array(coords.length);
            for (var i = 0; i < coords.length; i++) {
                latlngs[i] = new L.LatLng(coords[i][0], coords[i][1]);
            }
            return new L.Polyline(latlngs, options);
        };
    }
    function decode(str, precision) {
        var index = 0,
            lat = 0,
            lng = 0,
            coordinates = [],
            shift = 0,
            result = 0,
            byte = null,
            latitude_change,
            longitude_change,
            factor = Math.pow(10, precision || 5);
        while (index < str.length) {
            byte = null;
            shift = 0;
            result = 0;
            do {
                byte = str.charCodeAt(index++) - 63;
                result |= (byte & 0x1f) << shift;
                shift += 5;
            } while (byte >= 0x20);
            latitude_change = ((result & 1) ? ~(result >> 1) : (result >> 1));
            shift = result = 0;
            do {
                byte = str.charCodeAt(index++) - 63;
                result |= (byte & 0x1f) << shift;
                shift += 5;
            } while (byte >= 0x20);
            longitude_change = ((result & 1) ? ~(result >> 1) : (result >> 1));
            lat += latitude_change;
            lng += longitude_change;
            coordinates.push([lat / factor, lng / factor]);
        }
        return coordinates;
    }
})();

// --- Initialisierung ---
initializeMap();
