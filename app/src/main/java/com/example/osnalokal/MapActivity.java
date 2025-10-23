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
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MapActivity extends AppCompatActivity implements SensorEventListener, RouteSuggestionAdapter.OnRouteSuggestionClickListener {

    private WebView webView;
    private LocationManager locationManager;
    private final String GOOGLE_API_KEY = BuildConfig.GOOGLE_MAPS_API_KEY;
    private boolean isPageLoaded = false;

    private SensorManager sensorManager;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    private long lastHeadingUpdateTime = 0;

    // --- Neue UI-Komponenten und Datenlisten ---
    private RecyclerView routeSuggestionsRecyclerView;
    private RouteSuggestionAdapter routeSuggestionAdapter;
    private List<MapRouteSuggestion> currentSuggestions = new ArrayList<>();
    private List<Route> filteredRoutes = new ArrayList<>(); // Hält alle gefilterten Routen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        locationManager = LocationManager.getInstance(this);

        webView = findViewById(R.id.mapWebView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setDomStorageEnabled(true);

        routeSuggestionsRecyclerView = findViewById(R.id.recycler_view_route_suggestions);

        ImageView iconBack = findViewById(R.id.icon_back);
        iconBack.setOnClickListener(v -> finish());

        FloatingActionButton fab = findViewById(R.id.fab_center_on_user);
        fab.setOnClickListener(v -> webView.evaluateJavascript("javascript:centerOnUserLocation()", null));

        webView.addJavascriptInterface(new WebAppInterface(), "Android");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100 && !isPageLoaded) {
                    isPageLoaded = true;
                    android.location.Location lastLocation = locationManager.getLastKnownLocation();
                    if (lastLocation != null) {
                        String javascriptUserPos = "javascript:updateUserLocationFromApp(" + lastLocation.getLatitude() + ", " + lastLocation.getLongitude() + ")";
                        webView.evaluateJavascript(javascriptUserPos, null);
                    }
                    prepareDataFromIntent();
                }
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        webView.loadUrl("file:///android_asset/map.html");
        setupEdgeToEdge();
    }

    private void prepareDataFromIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra("FILTER_CRITERIA")) {
            FilterCriteria criteria = (FilterCriteria) intent.getSerializableExtra("FILTER_CRITERIA");
            handleFilteredRoutes(criteria);
        } else if (intent.hasExtra("SINGLE_ROUTE_IDS")) {
            ArrayList<Integer> locationIds = intent.getIntegerArrayListExtra("SINGLE_ROUTE_IDS");
            String routeName = intent.getStringExtra("ROUTE_NAME");
            handleSingleRoute(locationIds, routeName);
        } else {
            Toast.makeText(this, "Keine Routendaten gefunden.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

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
        loadLocationsIntoWebView(waypoints, true);
        calculateAndDrawRoute(waypoints, -1);
    }

    private void handleFilteredRoutes(FilterCriteria criteria) {
        // KORREKTUR: Das Argument 'this' wurde entfernt.
        List<Route> allRoutes = RoutesData.getAllRoutes();

        this.filteredRoutes = allRoutes.stream()
                .filter(route -> criteria.minDurationHours == null || (route.getDurationInMinutes() / 60.0) >= criteria.minDurationHours)
                .filter(route -> criteria.maxDurationHours == null || (route.getDurationInMinutes() / 60.0) <= criteria.maxDurationHours)
                .filter(route -> criteria.budget == null)
                .filter(route -> criteria.categories == null || criteria.categories.isEmpty() || criteria.categories.contains(route.getCategory()))
                .filter(route -> {
                    if (criteria.restaurantTags == null || criteria.restaurantTags.isEmpty()) return true;
                    if (route.getTags() == null) return false;
                    return route.getTags().stream().anyMatch(criteria.restaurantTags::contains);
                })
                .collect(Collectors.toList());

        if (this.filteredRoutes.isEmpty()) {
            Toast.makeText(this, "Keine Routen für diese Filter gefunden.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        currentSuggestions.clear();
        for (Route r : this.filteredRoutes) {
            currentSuggestions.add(new MapRouteSuggestion(r, getLocationsString(r), "...", "..."));
        }

        setupRouteSuggestionsList();
        Route activeRoute = this.filteredRoutes.get(0);
        handleRouteSelection(activeRoute);
    }

    private void handleRouteSelection(Route route) {
        updateToolbar(route.getName(), route.getLocationIds().size());
        updateMapMarkersForSelection(route);
        List<Location> waypoints = findWaypointsByIds(route.getLocationIds());
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
                android.util.Log.e("MapActivity", "Fehler bei der Routenberechnung: " + e.getMessage(), e);
            }
        }).start();
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

    private void updateMapMarkersForSelection(Route selectedRoute) {
        if (!isPageLoaded) return;

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

    private List<Location> findWaypointsByIds(List<Integer> locationIds) {
        List<Location> allLocations = LocationsData.getAllLocations(this);
        List<Location> waypoints = new ArrayList<>();
        if (locationIds != null) {
            for (Integer id : locationIds) {
                allLocations.stream()
                        .filter(loc -> loc.getId() == id)
                        .findFirst()
                        .ifPresent(waypoints::add);
            }
        }
        return waypoints;
    }

    private String getLocationsString(Route route) {
        List<Location> locations = findWaypointsByIds(route.getLocationIds());
        return locations.stream().map(Location::getName).collect(Collectors.joining(", "));
    }

    private void updateToolbar(String title, int stops) {
        // KORREKTUR: Diese Aufrufe funktionieren jetzt, da die IDs im XML existieren.
        TextView tvRouteTitle = findViewById(R.id.tv_route_title);
        TextView tvStopsCount = findViewById(R.id.tv_stops_count);
        tvRouteTitle.setText(title);
        tvStopsCount.setText(stops + " Stopps");
    }

    private void loadLocationsIntoWebView(List<Location> waypoints, boolean isActive) {
        if (waypoints == null || waypoints.isEmpty() || !isPageLoaded) return;
        String json = new Gson().toJson(waypoints);
        String escapedJson = json.replace("'", "\\'");
        String javascript = "javascript:loadLocationsFromApp('" + escapedJson + "', " + isActive + ")";
        webView.evaluateJavascript(javascript, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.length);
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastHeadingUpdateTime > 100) {
            updateOrientationAngles();
            lastHeadingUpdateTime = currentTime;
        }
    }

    public void updateOrientationAngles() {
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
        SensorManager.getOrientation(rotationMatrix, orientationAngles);
        float azimuthInDegrees = (float) Math.toDegrees(orientationAngles[0]);
        azimuthInDegrees = (azimuthInDegrees + 360) % 360;
        if (webView != null && isPageLoaded) {
            webView.evaluateJavascript("javascript:updateUserHeading(" + azimuthInDegrees + ")", null);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.map), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            findViewById(R.id.appBarLayout).setPadding(0, systemBars.top, 0, 0);
            return insets;
        });
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void onMarkerClick(int locationId) {
            Location clickedLocation = findLocationById(locationId);
            if (clickedLocation != null) {
                runOnUiThread(() -> DetailBottomSheetFragment.newInstance(
                        clickedLocation.getName(),
                        clickedLocation.getBeschreibung(),
                        clickedLocation.getArt(),
                        String.valueOf(clickedLocation.getBewertungen()),
                        clickedLocation.getOeffnungszeiten(),
                        clickedLocation.getBudgetAsEuroString(),
                        R.drawable.rec_tours_testimg
                ).show(getSupportFragmentManager(), "DetailBottomSheetFromMap"));
            }
        }

        private Location findLocationById(int id) {
            return LocationsData.getAllLocations(MapActivity.this).stream()
                    .filter(loc -> loc.getId() == id)
                    .findFirst()
                    .orElse(null);
        }
    }
}
