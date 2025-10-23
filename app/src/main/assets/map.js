
let map;
let userMarker = null;let hasZoomedToUser = false;
let allLocations = [];
let locationMarkers = [];

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
    iconUrl: 'file:///android_res/drawable/directedlocationmarker.png',

    iconSize:     [38, 38],
    iconAnchor:   [19, 19],
    popupAnchor:  [0, -19]
});

const greyPin = L.icon({
    iconUrl: 'file:///android_res/drawable/mappin_grey.png',

    iconSize:     [38, 38],
    iconAnchor:   [19, 38]
});


function initializeMap() {
    console.log("Initialisiere die Karte...");

    map = L.map('map');
    map.setView([52.2790, 8.0425], 15);

    L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
        maxZoom: 19,
        attribution: '© OpenStreetMap contributors, © CARTO'
    }).addTo(map);
}

function loadLocationsFromApp(jsonString, isActive) {
    console.log(`Lade Locations. Aktiv: ${isActive}`);
    const locations = JSON.parse(jsonString);

    const pinIcon = isActive ? defaultPin : greyPin;

    locations.forEach(location => {
        const marker = L.marker([location.breitengrad, location.laengengrad], { icon: pinIcon })
            .addTo(map)
            .on('click', () => {
                window.Android.onMarkerClick(location.id);
            });
    });
}

function resetAllMarkers() {
    locationMarkers.forEach(m => m.setIcon(defaultPin));
}

function addMarkersForLocations() {
    console.log(`Füge ${allLocations.length} Marker hinzu...`);
    if (!map || !allLocations) return;

    locationMarkers.forEach(m => map.removeLayer(m));
    locationMarkers = [];

    allLocations.forEach(location => {
        if (location.breitengrad && location.laengengrad) {
            const marker = L.marker([location.breitengrad, location.laengengrad], {icon: defaultPin}).addTo(map);

            locationMarkers.push(marker);

            marker.on('click', function() {
                resetAllMarkers();

                marker.setIcon(selectedPin);

                map.panTo(marker.getLatLng());

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
        map.flyTo(userMarker.getLatLng(), 17);
    } else {
        console.log("User-Position ist noch nicht bekannt.");
    }
}

function updateUserLocationFromApp(lat, lng) {
    console.log(`User-Position von App empfangen: ${lat}, ${lng}`);
    const userLatLng = [lat, lng];

    if (userMarker) {
        userMarker.setLatLng(userLatLng);
    } else {
        userMarker = L.marker(userLatLng, {
            icon: userPin,
            rotationAngle: 0,
            rotationOrigin: 'center center'
        }).addTo(map);
        userMarker.bindPopup("<b>Dein Standort</b>");
    }
}

function updateUserHeading(heading) {
    if (userMarker) {
        userMarker.setRotationAngle(heading);
    }
}

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

    locationMarkers.forEach(m => map.removeLayer(m));

    fetchAndDrawGoogleRoute(startPoint, endPoint, apiKey);
}


async function fetchAndDrawGoogleRoute(origin, destination, apiKey) {
    console.log(`Rufe Google Directions API ab für: ${origin.name} -> ${destination.name}`);

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

        const distanceText = leg.distance.text;
        const durationText = leg.duration.text;

        console.log(`Distanz: ${distanceText}, Dauer: ${durationText}`);
        alert(`Geschätzte Route:\nDistanz: ${distanceText}\nDauer: ${durationText}`);

        const encodedPolyline = route.overview_polyline.points;
        const decodedCoords = decodeGooglePolyline(encodedPolyline);

        drawRoutePolyline(decodedCoords);
        addStartEndMarkers(origin, destination);

    } catch (error) {
        console.error("Fehler beim Abrufen/Verarbeiten der Google Directions:", error);
        alert("Fehler bei der Routenplanung. Prüfe Internetverbindung und API Key.");
    }
}


function drawRoutePolyline(coordinates) {
    if (currentRouteLayer) {
        map.removeLayer(currentRouteLayer);
    }
    if (!coordinates || coordinates.length === 0) return;
    console.log("Zeichne Google Route Polyline...");
    currentRouteLayer = L.polyline(coordinates, {
        color: '#4A89F3',
        weight: 6,
        opacity: 0.8
    }).addTo(map);

    map.fitBounds(currentRouteLayer.getBounds());
}

function addStartEndMarkers(origin, destination) {
    startEndMarkers.forEach(m => map.removeLayer(m));
    startEndMarkers = [];

    const startMarker = L.marker([origin.lat, origin.lng]).addTo(map).bindPopup("Start");
    const endMarker = L.marker([destination.lat, destination.lng]).addTo(map).bindPopup("Ziel");

    startEndMarkers.push(startMarker, endMarker);
}

// Füge diese globale Variable am Anfang der Datei hinzu
let currentDrawnMarkers = [];

// Diese Funktion löscht alle Marker, die von loadLocationsFromApp erstellt wurden
function clearAllMarkers() {
    currentDrawnMarkers.forEach(marker => {
        map.removeLayer(marker);
    });
    currentDrawnMarkers = [];
}

// Passe deine 'loadLocationsFromApp' Funktion leicht an
function loadLocationsFromApp(jsonString, isActive) {
    console.log(`Lade Locations. Aktiv: ${isActive}`);
    try {
        const locations = JSON.parse(jsonString);
        const pinIcon = isActive ? defaultPin : greyPin;

        locations.forEach(location => {
            const marker = L.marker([location.breitengrad, location.laengengrad], { icon: pinIcon })
                .addTo(map)
                .on('click', () => {
                    // Marker-Klick an Android senden
                    if (window.Android && typeof window.Android.onMarkerClick === 'function') {
                       window.Android.onMarkerClick(location.id);
                    }
                });
            // Füge den neuen Marker zur Liste hinzu, um ihn später löschen zu können
            currentDrawnMarkers.push(marker);
        });
    } catch (e) {
        console.error("Fehler beim Parsen der Location-Daten:", e, jsonString);
    }
}



function decodeGooglePolyline(encoded) {
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

let currentRouteLayer = null;

function drawRouteFromEncodedPath(encodedPath) {
    console.log("Zeichne Route aus Java mit encodedPath...");

    if (currentRouteLayer) {
        map.removeLayer(currentRouteLayer);
    }

    const decodedCoords = decodeGooglePolyline(encodedPath);

    if (!decodedCoords || decodedCoords.length === 0) {
        console.error("Dekodierung fehlgeschlagen oder keine Koordinaten.");
        return;
    }

    currentRouteLayer = L.polyline(decodedCoords, {
        color: '#4A89F3',
        weight: 6,
        opacity: 0.85
    }).addTo(map);

    map.fitBounds(currentRouteLayer.getBounds().pad(0.1));
}

initializeMap();
