// --- Globale Variablen ---
let map;
let userMarker = null;
let hasZoomedToUser = false;

function initializeMap() {
    console.log("Initialisiere die Karte...");

    map = L.map('map');

    //Default View auf Osnabrück
    map.setView([52.2790, 8.0425], 15);

    // Fügt die Kartenkacheln hinzu
    L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
        maxZoom: 19,
        attribution: '© OpenStreetMap contributors, © CARTO'
    }).addTo(map);

    // Startet die Standortüberwachung
    startGeolocation();

    // Fügt die vordefinierten Pins für Osnabrück hinzu
    addOsnabrueckMarkers();
}

function startGeolocation() {
    console.log("Starte Standortüberwachung...");
    // Prüft, ob Geolocation im Browser verfügbar ist
    if (navigator.geolocation) {
        // "watchPosition" aktualisiert die Position, wenn der Nutzer sich bewegt
        navigator.geolocation.watchPosition(updateLocation, handleLocationError);
    }
}

// Wird aufgerufen, wenn eine neue Position erfolgreich ermittelt wurde
function updateLocation(pos) {
    const lat = pos.coords.latitude;
    const lng = pos.coords.longitude;
    const accuracy = pos.coords.accuracy;

    console.log(`Neue Position: ${lat}, ${lng} (Genauigkeit: ${accuracy}m)`);

    // Wenn schon ein Marker existiert, wird nur seine Position aktualisiert
    if (userMarker) {
        userMarker.setLatLng([lat, lng]);
    } else {
        //userMarker = L.marker([lat, lng]).addTo(map);
        userMarker = L.marker([lat, lng], {icon: userPin}).addTo(map);

    }

    // Beim allerersten erfolgreichen Standort-Update: Fliege elegant zur Position des Nutzers.
    if (!hasZoomedToUser) {
        map.flyTo([lat, lng], 17); // Zoomt nah an die Position
        hasZoomedToUser = true;
    }
}

// Wird aufgerufen, wenn die Standort-Ermittlung fehlschlägt
function handleLocationError(err) {
    if (err.code === 1) {
        console.warn("Nutzer hat die Standortfreigabe verweigert.");
        // Die Karte bleibt auf der Standard-Ansicht (Osnabrück)
    } else {
        console.error(`Fehler bei der Standort-Ermittlung: ${err.message}`);
    }
}


//Paar Beispiel Pins für Innenstadt
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

//Initialisiere die Karte
initializeMap();
