// --- Globale Variablen ---
let map;
let userMarker = null;
let hasZoomedToUser = false;
let currentRouteLayer = null; // Um alte Routenlinien zu entfernen
let routeMarkers = []; // Um Start/End-Marker zu verwalten

//Erste Beispiel Tour Osnabrück
const osnaLocations = [
    {
        lat: 52.2775,
        lng: 8.0416,
        name: "Rathaus des Westfälischen Friedens",
        description: "Historischer Ort des Friedensschlusses von 1648."
    },
    {
        lat: 52.2790,
        lng: 8.0425,
        name: "Dom St. Peter",
        description: "Das Herz der Osnabrücker Altstadt."
    },
    {
        lat: 52.2766,
        lng: 8.0375,
        name: "Heger Tor",
        description: "Ein imposantes Stadttor und Denkmal."
    },
    {
        lat: 52.2758,
        lng: 8.0445,
        name: "L&T Markthalle",
        description: "Moderne Gastronomie trifft auf Shopping."
    }
];

//Funktion definiert eigene Pin Icons
const allPins = L.icon({
    iconUrl: 'file:///android_res/drawable/mappin.png',

    iconSize:     [38, 38],
    iconAnchor:   [19, 38],
    popupAnchor:  [0, -38]
});

const userPin = L.icon({
    iconUrl: 'file:///android_res/drawable/logopin.png',

    iconSize:     [38, 38],
    iconAnchor:   [19, 38],
    popupAnchor:  [0, -38]
});

// Funktion, die die Pins aus dem Array zur Karte hinzufügt
function addOsnabrueckMarkers() {
    console.log("Füge Osnabrück-Marker hinzu...");
    osnaLocations.forEach(location => {
        const marker = L.marker([location.lat, location.lng], {icon: allPins}).addTo(map);
        marker.bindPopup(`<b>${location.name}</b><br>${location.description}`);
    });
}

// --- NEUE Funktion zum Abrufen und Zeichnen der Google Route ---
async function fetchAndDrawGoogleRoute(origin, destination) {
    console.log("Rufe Google Directions API ab für:", origin.name, "->", destination.name);

    const apiKey = "AIzaSyAnOJX2k6RMbKDlkzhlPHZq8pD8cBeHh60";

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

        // *** HIER BRAUCHST DU DIE DEKODIER-FUNKTION FÜR GOOGLE ***
        const decodedCoords = decodeGooglePolyline(encodedPolyline);

        // Zeichne die neue Route und die Marker
        drawRoutePolyline(decodedCoords);
        addStartEndMarkers(origin, destination); // Nutzt jetzt die Location-Objekte

    } catch (error) {
        console.error("Fehler beim Abrufen/Verarbeiten der Google Directions:", error);
        alert("Fehler bei der Routenplanung. Prüfe Internetverbindung und API Key.");
    }
}

function decodeGooglePolyline(encoded) {
    let points = [];
    let index = 0, len = encoded.length;
    let lat = 0, lng = 0;

    while (index < len) {
        let b, shift = 0, result = 0;
        do {
            // Hole das nächste Zeichen und ziehe 63 ab (ASCII-Offset)
            b = encoded.charCodeAt(index++) - 63;
            // Nimm die unteren 5 Bits und füge sie zum Ergebnis hinzu
            result |= (b & 0x1f) << shift;
            shift += 5;
        } while (b >= 0x20); // Wiederholen, solange das 6. Bit gesetzt ist (Fortsetzungs-Bit)

        // Wandle das Ergebnis in die Breitenänderung um
        // Wenn das letzte Bit 1 ist, ist es eine negative Zahl (~ invertiert die Bits)
        const dlat = ((result & 1) ? ~(result >> 1) : (result >> 1));
        lat += dlat;

        shift = 0;
        result = 0;
        do {
            b = encoded.charCodeAt(index++) - 63;
            result |= (b & 0x1f) << shift;
            shift += 5;
        } while (b >= 0x20);

        // Wandle das Ergebnis in die Längenänderung um
        const dlng = ((result & 1) ? ~(result >> 1) : (result >> 1));
        lng += dlng;

        // Füge das Koordinatenpaar (geteilt durch 1e5 für Dezimalstellen) zum Array hinzu
        points.push([lat / 1e5, lng / 1e5]);
    }

    return points;
}

// --- Funktion zum Zeichnen der Polyline (wie im vorigen Vorschlag) ---
function drawRoutePolyline(coordinates) {
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
    map.fitBounds(currentRouteLayer.getBounds());
}

// --- Funktion zum Entfernen alter Marker ---
function clearRouteMarkers() {
    routeMarkers.forEach(marker => map.removeLayer(marker));
    routeMarkers = [];
}

// --- Funktion zum Hinzufügen von Start/End-Markern (angepasst) ---
function addStartEndMarkers(startLocation, endLocation) {
    clearRouteMarkers(); // Alte Start/End-Marker entfernen

    console.log("Füge Start/End-Marker hinzu...");
    const startMarker = L.marker([startLocation.lat, startLocation.lng], {icon: allPins})
        .addTo(map)
        .bindPopup(`<b>Start:</b><br>${startLocation.name}`);
    const endMarker = L.marker([endLocation.lat, endLocation.lng], {icon: allPins})
        .addTo(map)
        .bindPopup(`<b>Ziel:</b><br>${endLocation.name}`);

    routeMarkers.push(startMarker, endMarker); // Zum späteren Entfernen merken
}


function initializeMap() {
    console.log("Initialisiere die Karte...");
    map = L.map('map').setView([52.2790, 8.0425], 15);

    // Dein Kartenstil
    L.tileLayer('https://stamen-tiles-{s}.a.ssl.fastly.net/toner-lite/{z}/{x}/{y}{r}.png', {
         maxZoom: 19,
         attribution: 'Map tiles by Stamen Design, under CC BY 3.0. Data by OpenStreetMap, under ODbL.'
    }).addTo(map);

    startGeolocation(); // Standortüberwachung starten

    // --- ÄNDERUNG: Rufe die Google Route statt addOsnabrueckMarkers auf ---
    // Wir nehmen einfach die ersten beiden Orte aus deinen Mock-Daten als Beispiel
    if (osnaLocations.length >= 2) {
        fetchAndDrawGoogleRoute(osnaLocations[0], osnaLocations[1]); // z.B. Rathaus -> Dom
    } else {
        console.warn("Nicht genug Orte in osnaLocations für eine Route.");
        // Fallback: Zeige alle Mock-Daten-Marker an (deine alte Funktion)
        addOsnabrueckMarkers();
    }
}

// --- Standort-Funktionen (wie gehabt, aber mit userLocationIcon) ---
function startGeolocation() {
    console.log("Starte Standortüberwachung...");
    if (navigator.geolocation) {
        navigator.geolocation.watchPosition(updateLocation, handleLocationError);
    }
} //

function updateLocation(pos) {
    const lat = pos.coords.latitude;
    const lng = pos.coords.longitude;
    const accuracy = pos.coords.accuracy;
    console.log(`Neue Position: ${lat}, ${lng} (Genauigkeit: ${accuracy}m)`);

    if (userMarker) {
        userMarker.setLatLng([lat, lng]);
    } else {
        // Verwende dein User-Icon
        userMarker = L.marker([lat, lng], {icon: userPin}).addTo(map);
    }

    // Beim ersten Mal hinzoomen (optional, kannst du rausnehmen, wenn fitBounds reicht)
    /* if (!hasZoomedToUser) {
        // map.flyTo([lat, lng], 17); // Nicht unbedingt nötig, wenn fitBounds genutzt wird
        hasZoomedToUser = true;
    } */
} //

function handleLocationError(err) {
    if (err.code === 1) {
        console.warn("Nutzer hat die Standortfreigabe verweigert.");
    } else {
        console.error(`Fehler bei der Standort-Ermittlung: ${err.message}`);
    }
} //


// --- Deine alte Funktion zum Hinzufügen ALLER Marker (als Fallback behalten) ---
function addOsnabrueckMarkers() {
    console.log("Füge Osnabrück-Marker hinzu (Fallback)...");
    clearRouteMarkers(); // Sicherstellen, dass keine Routenmarker da sind
    osnaLocations.forEach(location => {
        const marker = L.marker([location.lat, location.lng], {icon: allPins}).addTo(map);
        // Google Maps Link (wie vorher)
        const googleMapsUrl = `https://www.google.com/maps/search/?api=1&query=${location.lat},${location.lng}`;
        const popupContent = `
            <b>${location.name}</b><br>
            ${location.description}<br><br>
            <a href="${googleMapsUrl}" target="_blank">Auf Google Maps öffnen</a>
        `;
        marker.bindPopup(popupContent);
        routeMarkers.push(marker); // Alle Marker merken zum Entfernen
    });
} //

// --- Initialisierung am Ende ---
initializeMap();