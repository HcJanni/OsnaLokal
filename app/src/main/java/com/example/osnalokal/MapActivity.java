package com.example.osnalokal;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MapActivity extends AppCompatActivity implements SensorEventListener, RouteSuggestionAdapter.OnRouteSuggestionClickListener {

    private WebView webView;
    private LocationManager locationManager;
    private final String GOOGLE_API_KEY = BuildConfig.GOOGLE_MAPS_API_KEY;
    private boolean isMapReady = false;

    // Lade die Locations und Routen EINMAL und halte sie hier.
    private List<Location> allLocations;
    private List<Route> allRoutes;

    // Sensor- und UI-Variablen
    private SensorManager sensorManager;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    private long lastHeadingUpdateTime = 0;
    private RecyclerView routeSuggestionsRecyclerView;
    private RouteSuggestionAdapter routeSuggestionAdapter;
    private List<MapRouteSuggestion> currentSuggestions = new ArrayList<>();
    private List<Route> filteredRoutes = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Lade ALLE Daten hier und nur hier, um wiederholtes Laden zu vermeiden.
        // DataManager ist ein Singleton, das die Daten nach dem ersten Laden zwischenspeichert.
        this.allLocations = DataManager.getInstance().getAllLocations();
        this.allRoutes = DataManager.getInstance().getAllRoutes();

        locationManager = LocationManager.getInstance(this);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        webView = findViewById(R.id.mapWebView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setDomStorageEnabled(true);
        WebView.setWebContentsDebuggingEnabled(true); // Wichtig für die Fehlersuche

        routeSuggestionsRecyclerView = findViewById(R.id.recycler_view_route_suggestions);
        ImageView iconBack = findViewById(R.id.icon_back);
        iconBack.setOnClickListener(v -> finish());
        FloatingActionButton fab = findViewById(R.id.fab_center_on_user);
        fab.setOnClickListener(v -> webView.evaluateJavascript("javascript:centerOnUserLocation()", null));

        // Richte die Schnittstelle für die Kommunikation von JS nach Java ein
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

            // Dieser Callback wird verwendet, um zu wissen, wann die Karte fertig geladen ist
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100 && !isMapReady) {
                    isMapReady = true;
                    // Sobald die Karte geladen ist, verarbeite die Intent-Daten
                    prepareDataFromIntent();
                }
            }
        });

        webView.loadUrl("file:///android_asset/map.html");
    }

    /**
     * Zentrale Methode, die entscheidet, was auf der Karte angezeigt wird,
     * basierend auf den Daten, die von der vorherigen Activity kommen.
     */
    private void prepareDataFromIntent() {
        Log.d("MapActivity", "prepareDataFromIntent aufgerufen, da die Karte bereit ist.");
        Intent intent = getIntent();

        if (intent.hasExtra("FILTER_CRITERIA")) {
            FilterCriteria criteria = (FilterCriteria) intent.getSerializableExtra("FILTER_CRITERIA");
            if (criteria != null) {
                if (criteria.isSingleCategoryMode()) {
                    // Szenario 1: Nur eine Kategorie -> Zeige alle passenden Orte an
                    displaySingleLocations(criteria);
                } else {
                    // Szenario 2: Mehrere Kategorien -> Generiere und zeige Routen
                    generateAndDisplayRoutes(criteria);
                }
            }
        } else if (intent.hasExtra("SINGLE_ROUTE_IDS")) {
            // Szenario 3: Bestehende Logik für vordefinierte Routen funktioniert weiterhin
            ArrayList<Integer> locationIds = intent.getIntegerArrayListExtra("SINGLE_ROUTE_IDS");
            String routeName = intent.getStringExtra("ROUTE_NAME");
            handleSingleRoute(locationIds, routeName);
        } else {
            Toast.makeText(this, "Keine Routendaten gefunden.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Szenario 1: Filtert Orte nach einer einzelnen Kategorie und zeigt sie als Marker auf der Karte an.
     */
    private void displaySingleLocations(FilterCriteria criteria) {
        routeSuggestionsRecyclerView.setVisibility(View.GONE); // Routenleiste ausblenden

        // KORREKTUR: Wir verwenden jetzt die schlaue Filter-Methode aus dem RouteGenerator
        // anstatt der simplen, alten Logik.
        List<Location> matchingLocations = RouteGenerator.findMatchingLocations(criteria);

        // Der Toolbar-Titel sollte jetzt generischer sein, da es auch Sub-Filter gibt
        String title = "Gefilterte Orte";
        if (!criteria.categories.isEmpty()) {
            title = criteria.categories.iterator().next();
        }
        updateToolbar(title, -1); // Toolbar-Titel anpassen

        // Update die Anzahl im Titel
        ((TextView) findViewById(R.id.tv_stops_count)).setText(matchingLocations.size() + " Orte gefunden");

        if (matchingLocations.isEmpty()) {
            Toast.makeText(this, "Keine Orte für diese Filterkombination gefunden.", Toast.LENGTH_LONG).show();
            return;
        }

        // Der Rest der Methode zum Anzeigen der Marker bleibt unverändert
        Gson gson = new Gson();
        String locationsJson = gson.toJson(matchingLocations);

        webView.evaluateJavascript("javascript:clearAll(); showLocationsAsMarkers('" + locationsJson.replace("'", "\\'") + "');", null);
    }

    /**
     * Szenario 2: Startet den RouteGenerator, um Routenvorschläge zu erstellen und anzuzeigen.
     */
    private void generateAndDisplayRoutes(FilterCriteria criteria) {
        routeSuggestionsRecyclerView.setVisibility(View.VISIBLE); // Routenleiste einblenden
        Toast.makeText(this, "Suche nach passenden Routen...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            RouteGenerator generator = new RouteGenerator(GOOGLE_API_KEY);
            // Rufe die neue Methode auf, die eine Liste zurückgibt
            List<Route> generatedRoutes = generator.generateRoutesFromFilter(criteria);

            runOnUiThread(() -> {
                if (generatedRoutes.isEmpty()) {
                    Toast.makeText(this, "Keine Route für diese Kriterien gefunden.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    this.filteredRoutes.clear();
                    this.filteredRoutes.addAll(generatedRoutes);

                    currentSuggestions.clear();
                    for (Route r : this.filteredRoutes) {
                        currentSuggestions.add(new MapRouteSuggestion(r, getLocationsString(r), "...", "..."));
                    }

                    setupRouteSuggestionsList();
                    if (!this.filteredRoutes.isEmpty()) {
                        handleRouteSelection(this.filteredRoutes.get(0));
                    }
                }
            });
        }).start();
    }


    /**
     * Die Schnittstelle, die es JavaScript (aus der WebView) erlaubt, Java-Methoden aufzurufen.
     */
    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void onLocationClicked(String locationJson) {
            runOnUiThread(() -> {
                Gson gson = new Gson();
                Location clickedLocation = gson.fromJson(locationJson, Location.class);
                DetailBottomSheetFragment.newInstance(clickedLocation).show(getSupportFragmentManager(), "DetailBottomSheet");
            });
        }

        @JavascriptInterface
        public void onMarkerClick(int locationId) {
            Location clickedLocation = findLocationById(locationId);
            if (clickedLocation != null) {
                runOnUiThread(() -> DetailBottomSheetFragment.newInstance(clickedLocation)
                        .show(getSupportFragmentManager(), "DetailBottomSheetFromMap"));
            }
        }
    }

    // Hilfsmethoden zum Finden von Orten
    private Location findLocationById(int id) {
        if (this.allLocations == null) return null;
        return this.allLocations.stream()
                .filter(loc -> loc.getId() == id)
                .findFirst()
                .orElse(null);
    }

    private List<Location> findWaypointsByIds(List<Integer> locationIds) {
        List<Location> waypoints = new ArrayList<>();
        if (locationIds == null || this.allLocations == null) return waypoints;

        for (Integer id : locationIds) {
            this.allLocations.stream()
                    .filter(loc -> loc.getId() == id)
                    .findFirst()
                    .ifPresent(waypoints::add);
        }
        if (waypoints.size() > 1) {
            waypoints.sort((l1, l2) -> Integer.compare(locationIds.indexOf(l1.getId()), locationIds.indexOf(l2.getId())));
        }
        return waypoints;
    }


    // --- Bestehende Logik für Routenanzeige ---
    // (Diese Methoden sind größtenteils unverändert, aber hier zur Vollständigkeit)

    @Override
    public void onRouteSuggestionClick(MapRouteSuggestion routeSuggestion, int position) {
        if (routeSuggestionAdapter != null) {
            routeSuggestionAdapter.setSelectedPosition(position);
        }
        Route selectedRoute = filteredRoutes.get(position);
        handleRouteSelection(selectedRoute);
    }

    private void handleSingleRoute(List<Integer> locationIds, String routeName) {
        updateToolbar(routeName, locationIds.size());
        List<Location> waypoints = findWaypointsByIds(locationIds);
        loadLocationsIntoWebView(waypoints, true); // Stellt alle Marker dar (aktiv)
        calculateAndDrawRoute(waypoints, -1);
    }

    private void handleRouteSelection(Route route) {
        // 1. Aktualisiere die Toolbar mit dem Namen und der Anzahl der Stopps der Route
        updateToolbar(route.getName(), route.getLocationIds().size());

        // 2. Aktualisiere die Marker auf der Karte (aktive/inaktive Darstellung)
        updateMapMarkersForSelection(route);

        // 3. Finde die Location-Objekte für die Route
        List<Location> waypoints = findWaypointsByIds(route.getLocationIds());

        // 4. Berechne und zeichne die Polyline für die Route auf der Karte
        int routeIndex = filteredRoutes.indexOf(route);
        calculateAndDrawRoute(waypoints, routeIndex);
    }

    private void calculateAndDrawRoute(List<Location> waypoints, final int routeIndexInAdapter) {
        if (waypoints == null || waypoints.size() < 2) return;
        new Thread(() -> {
            try {
                GeoApiContext context = new GeoApiContext.Builder().apiKey(GOOGLE_API_KEY).build();
                Location start = waypoints.get(0);
                Location end = waypoints.get(waypoints.size() - 1);
                com.google.maps.model.LatLng[] intermediatePoints = new com.google.maps.model.LatLng[waypoints.size() - 2];
                for (int i = 1; i < waypoints.size() - 1; i++) {
                    Location loc = waypoints.get(i);
                    intermediatePoints[i - 1] = new com.google.maps.model.LatLng(loc.getBreitengrad(), loc.getLaengengrad());
                }
                DirectionsResult result = DirectionsApi.newRequest(context)
                        .origin(new com.google.maps.model.LatLng(start.getBreitengrad(), start.getLaengengrad()))
                        .destination(new com.google.maps.model.LatLng(end.getBreitengrad(), end.getLaengengrad()))
                        .waypoints(intermediatePoints)
                        .mode(TravelMode.WALKING)
                        .await();

                if (result.routes != null && result.routes.length > 0) {
                    DirectionsRoute route = result.routes[0];
                    long totalDistanceInMeters = 0;
                    long totalDurationInSeconds = 0;
                    for (DirectionsLeg leg : route.legs) {
                        totalDistanceInMeters += leg.distance.inMeters;
                        totalDurationInSeconds += leg.duration.inSeconds;
                    }
                    final String totalDistanceString = String.format(java.util.Locale.GERMANY, "%.1f km", totalDistanceInMeters / 1000.0);
                    final String totalDurationString = String.format(java.util.Locale.GERMANY, "%d Min.", totalDurationInSeconds / 60);
                    final String encodedPath = route.overviewPolyline.getEncodedPath();
                    runOnUiThread(() -> {
                        String javascript = "javascript:drawRouteFromEncodedPath('" + encodedPath.replace("\\", "\\\\") + "')";
                        webView.evaluateJavascript(javascript, null);
                        if (routeSuggestionAdapter != null && routeIndexInAdapter >= 0 && routeIndexInAdapter < currentSuggestions.size()) {
                            MapRouteSuggestion suggestion = currentSuggestions.get(routeIndexInAdapter);
                            MapRouteSuggestion updatedSuggestion = new MapRouteSuggestion(
                                    suggestion.getOriginalRoute(),
                                    suggestion.getLocationsString(),
                                    totalDurationString,
                                    totalDistanceString
                            );
                            currentSuggestions.set(routeIndexInAdapter, updatedSuggestion);
                            routeSuggestionAdapter.notifyItemChanged(routeIndexInAdapter);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("MapActivity", "Fehler bei der Routenberechnung: " + e.getMessage(), e);
            }
        }).start();
    }

    private void updateMapMarkersForSelection(Route selectedRoute) {
        if (!isMapReady) return;
        List<Location> activeWaypoints = findWaypointsByIds(selectedRoute.getLocationIds());
        List<Location> allWaypoints = new ArrayList<>();
        for (Route route : filteredRoutes) {
            allWaypoints.addAll(findWaypointsByIds(route.getLocationIds()));
        }
        List<Location> uniqueWaypoints = allWaypoints.stream().distinct().collect(Collectors.toList());
        List<Location> inactiveWaypoints = uniqueWaypoints.stream().filter(p -> !activeWaypoints.contains(p)).collect(Collectors.toList());

        webView.evaluateJavascript("javascript:clearAllMarkers()", null);
        loadLocationsIntoWebView(inactiveWaypoints, false);
        loadLocationsIntoWebView(activeWaypoints, true);
    }

    private void loadLocationsIntoWebView(List<Location> locations, boolean isActive) {
        if (locations.isEmpty() || !isMapReady) return;
        Gson gson = new Gson();
        String json = gson.toJson(locations);
        String javascript = String.format("javascript:loadLocationsFromAppBase64('%s', %b)",
                android.util.Base64.encodeToString(json.getBytes(), android.util.Base64.NO_WRAP),
                isActive);
        webView.evaluateJavascript(javascript, null);
    }

    private void setupRouteSuggestionsList() {
        if (currentSuggestions.size() > 1) {
            routeSuggestionAdapter = new RouteSuggestionAdapter(currentSuggestions, this);
            routeSuggestionsRecyclerView.setAdapter(routeSuggestionAdapter);
            routeSuggestionsRecyclerView.setVisibility(View.VISIBLE);
        } else {
            routeSuggestionsRecyclerView.setVisibility(View.GONE);
        }
    }

    private String getLocationsString(Route route) {
        List<Location> locations = findWaypointsByIds(route.getLocationIds());
        return locations.stream().map(Location::getName).collect(Collectors.joining(" • "));
    }

    private void updateToolbar(String title, int stops) {
        ((TextView) findViewById(R.id.tv_route_title)).setText(title);
        TextView stopsTv = findViewById(R.id.tv_stops_count);
        if (stops >= 0) {
            stopsTv.setText(stops + (stops == 1 ? " Stop" : " Stops"));
            stopsTv.setVisibility(View.VISIBLE);
        } else {
            stopsTv.setVisibility(View.GONE); // Wird für den Einzel-Punkte-Modus versteckt
        }
    }


    // --- Lebenszyklus- und Sensor-Methoden ---
    @Override
    protected void onResume() {
        super.onResume();
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_UI);
        }
        locationManager.startLocationUpdates(this, location -> { // "this" ist der Context
            if (isMapReady) {
                webView.evaluateJavascript(String.format(java.util.Locale.US, "javascript:updateUserLocationFromApp(%f, %f)",
                                location.getLatitude(),
                                location.getLongitude()),
                        null);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        locationManager.stopLocationUpdates();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (System.currentTimeMillis() - lastHeadingUpdateTime < 100) return;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.length);
        }
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
        SensorManager.getOrientation(rotationMatrix, orientationAngles);
        float heading = (float) Math.toDegrees(orientationAngles[0]);
        if (isMapReady) {
            webView.evaluateJavascript("javascript:updateUserHeading(" + heading + ")", null);
            lastHeadingUpdateTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Nicht benötigt
    }
}

