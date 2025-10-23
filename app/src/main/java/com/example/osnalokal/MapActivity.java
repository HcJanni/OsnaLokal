package com.example.osnalokal;

import android.content.Intent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MapActivity extends AppCompatActivity implements SensorEventListener {

    private WebView webView;
    private LocationManager locationManager;
    private final String GOOGLE_API_KEY = BuildConfig.GOOGLE_MAPS_API_KEY;
    private List<Location> activeWaypointsToLoad = null;
    private List<List<Location>> inactiveWaypointsToLoad = new ArrayList<>();
    private boolean isPageLoaded = false;

    // VARIABLEN FÜR DEN KOMPASS
    private SensorManager sensorManager;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    private long lastHeadingUpdateTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        locationManager = LocationManager.getInstance(this);

        // --- 1. WebView und UI initialisieren ---
        webView = findViewById(R.id.mapWebView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setDomStorageEnabled(true);

        ImageView iconBack = findViewById(R.id.icon_back);
        iconBack.setOnClickListener(v -> finish());

        FloatingActionButton fab = findViewById(R.id.fab_center_on_user);
        fab.setOnClickListener(v -> webView.evaluateJavascript("javascript:centerOnUserLocation()", null));

        // --- 2. JavaScript-Brücke und WebChromeClient einrichten (vereinfacht) ---
        webView.addJavascriptInterface(new WebAppInterface(), "Android");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // Wenn die Seite fertig geladen ist, können wir den Standort des Nutzers setzen
                if (newProgress == 100) {
                    isPageLoaded = true;
                    android.location.Location lastLocation = locationManager.getLastKnownLocation();
                    if (lastLocation != null) {
                        String javascriptUserPos = "javascript:updateUserLocationFromApp(" + lastLocation.getLatitude() + ", " + lastLocation.getLongitude() + ")";
                        webView.evaluateJavascript(javascriptUserPos, null);
                    }
                    loadDataIntoWebViewIfReady();
                }
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, false, false); // JS soll sich nicht um Berechtigungen kümmern
            }
        });

        // --- 3. WebView laden ---
        webView.loadUrl("file:///android_asset/map.html");

        // --- 4. Hauptlogik starten: Unterscheide den Start-Modus ---
        Intent intent = getIntent();
        if (intent.hasExtra("FILTER_CRITERIA")) {
            FilterCriteria criteria = (FilterCriteria) intent.getSerializableExtra("FILTER_CRITERIA");
            handleFilteredRoutes(criteria);
        } else if (intent.hasExtra("SINGLE_ROUTE_IDS")) {
            List<Integer> locationIds = (List<Integer>) intent.getSerializableExtra("SINGLE_ROUTE_IDS");
            String routeName = intent.getStringExtra("ROUTE_NAME");
            handleSingleRoute(locationIds, routeName);
        } else {
            // Fallback, falls die Activity ohne Daten gestartet wird
            Toast.makeText(this, "Keine Routendaten gefunden.", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupEdgeToEdge();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Registriere die Listener für die Sensoren
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Deregistriere die Listener, um Akku zu sparen, wenn die App im Hintergrund ist
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.length);
        }

        // Aktualisiere die Richtung nur alle 100ms, um das System nicht zu überlasten
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastHeadingUpdateTime > 100) {
            updateOrientationAngles();
            lastHeadingUpdateTime = currentTime;
        }
    }

    public void updateOrientationAngles() {
        // Berechne die Rotationsmatrix
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);

        // Erhalte die Orientierungswinkel
        SensorManager.getOrientation(rotationMatrix, orientationAngles);

        // Der Azimut (die Richtung) ist der erste Wert. Er ist in Radian.
        // Konvertiere ihn in Grad.
        float azimuthInDegrees = (float) Math.toDegrees(orientationAngles[0]);

        // Normalisiere den Wert auf 0-360 Grad (Norden = 0)
        azimuthInDegrees = (azimuthInDegrees + 360) % 360;

        // Sende den Wert an das WebView
        if (webView != null && isPageLoaded) {
            String javascript = "javascript:updateUserHeading(" + azimuthInDegrees + ")";
            webView.evaluateJavascript(javascript, null);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Kann ignoriert werden, ist aber für das Interface notwendig
    }

    private void setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.map), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void calculateAndDrawRoute(List<Location> waypoints) {
        if (waypoints == null || waypoints.size() < 2) {
            android.util.Log.d("MapActivity", "Nicht genügend Wegpunkte für eine Route.");
            return;
        }

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
                        TextView tvRouteDetails = findViewById(R.id.tv_route_details);
                        tvRouteDetails.setText(totalDistanceString + " • ca. " + totalDurationString);
                        String javascript = "javascript:drawRouteFromEncodedPath('" + encodedPath.replace("\\", "\\\\") + "')";
                        if (isPageLoaded) {
                            // Wenn ja, führe den Befehl sofort aus
                            webView.evaluateJavascript(javascript, null);
                        } else {
                            webView.evaluateJavascript(javascript, null);
                            webView.evaluateJavascript(javascript, null);
                        }
                    });
                }
            } catch (Exception e) {
                android.util.Log.e("MapActivity", "Fehler bei der Routenberechnung: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void handleSingleRoute(List<Integer> locationIds, String routeName) {
        List<Location> waypoints = findWaypointsByIds(locationIds);
        updateToolbar(routeName, waypoints.size());

        this.activeWaypointsToLoad = waypoints; // Daten für später speichern
        loadDataIntoWebViewIfReady(); // Versuche, die Daten zu laden

        calculateAndDrawRoute(waypoints);
    }

    private void handleFilteredRoutes(FilterCriteria criteria) {
        List<Route> allRoutes = RoutesData.getAllRoutes();
        List<Route> filteredRoutes = allRoutes.stream()
                .filter(route -> criteria.minDurationHours == null || (route.getDurationInMinutes() / 60.0) >= criteria.minDurationHours)
                .filter(route -> criteria.maxDurationHours == null || (route.getDurationInMinutes() / 60.0) <= criteria.maxDurationHours)
                .filter(route -> criteria.budget == null)
                .filter(route -> criteria.categories.isEmpty() || criteria.categories.contains(route.getCategory()))
                .filter(route -> criteria.restaurantTags.isEmpty() || route.getTags().stream().anyMatch(tag -> criteria.restaurantTags.contains(tag)))

                .collect(Collectors.toList());

        if (filteredRoutes.isEmpty()) {
            Toast.makeText(this, "Keine Routen für diese Filter gefunden.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // TODO: Hier deine Sortierlogik für die "beste" Route einfügen
        Route activeRoute = filteredRoutes.get(0);
        updateToolbar(activeRoute.getName(), activeRoute.getLocationIds().size());

        this.activeWaypointsToLoad = findWaypointsByIds(activeRoute.getLocationIds());
        for (int i = 1; i < filteredRoutes.size(); i++) {
            this.inactiveWaypointsToLoad.add(findWaypointsByIds(filteredRoutes.get(i).getLocationIds()));
        }

        //List<Location> activeWaypoints = findWaypointsByIds(activeRoute.getLocationIds());
        loadDataIntoWebViewIfReady();
        calculateAndDrawRoute(activeWaypointsToLoad);
    }

    private void loadDataIntoWebViewIfReady() {
        // Führe den Code nur aus, wenn die Seite geladen ist UND es Daten zum Laden gibt
        if (isPageLoaded && activeWaypointsToLoad != null) {
            // Lade die aktiven Pins
            loadLocationsIntoWebView(activeWaypointsToLoad, true);

            // Lade alle inaktiven Pins
            for (List<Location> inactiveList : inactiveWaypointsToLoad) {
                loadLocationsIntoWebView(inactiveList, false);
            }

            // Setze die Daten zurück, damit sie nicht doppelt geladen werden
            activeWaypointsToLoad = null;
            inactiveWaypointsToLoad.clear();
        }
    }

    private List<Location> findWaypointsByIds(List<Integer> locationIds) {
        List<Location> allLocations = LocationsData.getAllLocations(this);
        List<Location> waypoints = new ArrayList<>();
        if (locationIds != null) {
            for (Integer id : locationIds) {
                for (Location loc : allLocations) {
                    if (loc.getId() == id) {
                        waypoints.add(loc);
                        break;
                    }
                }
            }
        }
        return waypoints;
    }

    private void updateToolbar(String title, int stops) {
        TextView tvRouteTitle = findViewById(R.id.tv_route_title);
        TextView tvStopsCount = findViewById(R.id.tv_stops_count);
        tvRouteTitle.setText(title);
        tvStopsCount.setText(stops + " Stopps");
    }

    private void loadLocationsIntoWebView(List<Location> waypoints, boolean isActive) {
        String json = new Gson().toJson(waypoints);
        // HINWEIS: Dein JavaScript braucht eine Funktion wie `loadLocations(json, isActive)`
        String javascript = "javascript:loadLocationsFromApp('" + json.replace("'", "\\'") + "', " + isActive + ")";
        webView.evaluateJavascript(javascript, null);
    }

    // Vereinfachte JavaScript-Brücke, die nur noch auf Klicks reagiert
    public class WebAppInterface {
        @JavascriptInterface
        public void onMarkerClick(int locationId) {
            // Hier muss die Logik hin, um das BottomSheet zu zeigen
            // Wir brauchen eine Methode, um die Location-Daten zu bekommen
            Location clickedLocation = findLocationById(locationId);
            if (clickedLocation != null) {
                runOnUiThread(() -> {
                    DetailBottomSheetFragment bottomSheet = DetailBottomSheetFragment.newInstance(
                            clickedLocation.getName(),
                            clickedLocation.getBeschreibung(),
                            clickedLocation.getArt(),
                            String.valueOf(clickedLocation.getBewertungen()),
                            clickedLocation.getOeffnungszeiten(),
                            clickedLocation.getBudgetAsEuroString(),
                            R.drawable.rec_tours_testimg
                    );
                    bottomSheet.show(getSupportFragmentManager(), "DetailBottomSheetFromMap");
                });
            }
        }

        private Location findLocationById(int id) {
            List<Location> allLocations = LocationsData.getAllLocations(MapActivity.this);
            for (Location loc : allLocations) {
                if (loc.getId() == id) {
                    return loc;
                }
            }
            return null;
        }
    }
}
