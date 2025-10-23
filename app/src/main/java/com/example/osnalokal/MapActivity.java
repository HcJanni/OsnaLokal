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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
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

    private List<Location> allLocations;
    private List<Route> allRoutes;

    private SensorManager sensorManager;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    private long lastHeadingUpdateTime = 0;

    private RecyclerView routeInfoRecyclerView;
    private View startRouteButtonContainer; // Nur noch diese eine Referenz für die unteren Buttons
    private int selectedRouteIndex = 0;
    private RouteSuggestionAdapter routeSuggestionAdapter;
    private List<MapRouteSuggestion> currentSuggestions = new ArrayList<>();
    private List<Route> filteredRoutes = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        this.allLocations = DataManager.getInstance().getAllLocations();
        this.allRoutes = DataManager.getInstance().getAllRoutes();

        locationManager = LocationManager.getInstance(this);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        webView = findViewById(R.id.mapWebView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setDomStorageEnabled(true);
        WebView.setWebContentsDebuggingEnabled(true);

        routeInfoRecyclerView = findViewById(R.id.recycler_view_route_info);
        startRouteButtonContainer = findViewById(R.id.start_route_button_container);

        Button startRouteButton = startRouteButtonContainer.findViewById(R.id.reusable_button_finish);
        startRouteButton.setText("Diese Route starten");
        startRouteButton.setOnClickListener(v -> finalizeSelectedRoute());

        ImageView iconBack = findViewById(R.id.icon_back);
        iconBack.setOnClickListener(v -> finish());
        FloatingActionButton fab = findViewById(R.id.fab_center_on_user);
        fab.setOnClickListener(v -> webView.evaluateJavascript("javascript:centerOnUserLocation()", null));

        webView.addJavascriptInterface(new WebAppInterface(this), "Android");

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100 && !isMapReady) {
                    isMapReady = true;
                    prepareDataFromIntent();
                }
            }
        });

        webView.loadUrl("file:///android_asset/map.html");
    }

    private void finalizeSelectedRoute() {
        if (filteredRoutes.isEmpty() || selectedRouteIndex >= filteredRoutes.size()) {
            return;
        }

        Route finalRoute = filteredRoutes.get(selectedRouteIndex);
        MapRouteSuggestion finalSuggestion = currentSuggestions.get(selectedRouteIndex);

        currentSuggestions.clear();
        currentSuggestions.add(finalSuggestion);

        startRouteButtonContainer.setVisibility(View.GONE);
        routeInfoRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        routeSuggestionAdapter = new RouteSuggestionAdapter(currentSuggestions, this);
        routeInfoRecyclerView.setAdapter(routeSuggestionAdapter);

        List<Location> finalWaypoints = findWaypointsByIds(finalRoute.getLocationIds());
        webView.evaluateJavascript("javascript:clearAllMarkers()", null);
        loadLocationsIntoWebView(finalWaypoints, true);
    }

    private void prepareDataFromIntent() {
        Intent intent = getIntent();

        if (intent.hasExtra("FILTER_CRITERIA")) {
            FilterCriteria criteria = (FilterCriteria) intent.getSerializableExtra("FILTER_CRITERIA");
            if (criteria != null) {
                if (criteria.isSingleCategoryMode()) {
                    displaySingleLocations(criteria);
                } else {
                    generateAndDisplayRoutes(criteria);
                }
            }
        } else if (intent.hasExtra("SINGLE_ROUTE_IDS")) {
            ArrayList<Integer> locationIds = intent.getIntegerArrayListExtra("SINGLE_ROUTE_IDS");
            String routeName = intent.getStringExtra("ROUTE_NAME");

            Route originalRoute = allRoutes.stream()
                    .filter(r -> r.getName().equals(routeName) && new ArrayList<>(r.getLocationIds()).equals(locationIds))
                    .findFirst()
                    .orElse(null);

            if (originalRoute != null) {
                handleSingleRoute(originalRoute);
            } else {
                Toast.makeText(this, "Routendetails konnten nicht geladen werden.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Keine Routendaten gefunden.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displaySingleLocations(FilterCriteria criteria) {
        // KORREKTUR: Der "Starten"-Button ist hier nicht relevant und bleibt unsichtbar.
        routeInfoRecyclerView.setVisibility(View.GONE);
        startRouteButtonContainer.setVisibility(View.GONE);

        List<Location> matchingLocations = RouteGenerator.findMatchingLocations(criteria);

        String title = "Gefilterte Orte";
        if (!criteria.categories.isEmpty()) {
            title = criteria.categories.iterator().next();
        }
        updateToolbar(title, -1);

        ((TextView) findViewById(R.id.tv_stops_count)).setText(matchingLocations.size() + " Orte gefunden");

        if (matchingLocations.isEmpty()) {
            Toast.makeText(this, "Keine Orte für diese Filterkombination gefunden.", Toast.LENGTH_LONG).show();
            return;
        }

        Gson gson = new Gson();
        String locationsJson = gson.toJson(matchingLocations);

        webView.evaluateJavascript("javascript:clearAll(); showLocationsAsMarkers('" + locationsJson.replace("'", "\\'") + "');", null);
    }

    private void generateAndDisplayRoutes(FilterCriteria criteria) {
        startRouteButtonContainer.setVisibility(View.VISIBLE);
        routeInfoRecyclerView.setVisibility(View.VISIBLE);
        routeInfoRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        Toast.makeText(this, "Suche nach passenden Routen...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            RouteGenerator generator = new RouteGenerator(GOOGLE_API_KEY);
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
                        handleRouteSelection(this.filteredRoutes.get(0), 0);
                    }
                }
            });
        }).start();
    }

    private void handleSingleRoute(Route route) {
        startRouteButtonContainer.setVisibility(View.GONE);
        routeInfoRecyclerView.setVisibility(View.VISIBLE);
        routeInfoRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        updateToolbar(route.getName(), route.getLocationIds().size());

        currentSuggestions.clear();
        currentSuggestions.add(new MapRouteSuggestion(route, getLocationsString(route), "...", "..."));

        routeSuggestionAdapter = new RouteSuggestionAdapter(currentSuggestions, this);
        routeInfoRecyclerView.setAdapter(routeSuggestionAdapter);

        List<Location> waypoints = findWaypointsByIds(route.getLocationIds());
        loadLocationsIntoWebView(waypoints, true);
        calculateAndDrawRoute(waypoints, 0);
    }

    public class WebAppInterface {
        Context mContext;
        WebAppInterface(Context c) { mContext = c; }
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

    @Override
    public void onRouteSuggestionClick(MapRouteSuggestion routeSuggestion, int position) {
        if (filteredRoutes.size() > 1 && routeSuggestionAdapter != null) {
            handleRouteSelection(routeSuggestion.getOriginalRoute(), position);
        }
    }

    private void handleRouteSelection(Route route, int position) {
        this.selectedRouteIndex = position;
        if (routeSuggestionAdapter != null) {
            routeSuggestionAdapter.setSelectedPosition(position);
        }
        updateToolbar(route.getName(), route.getLocationIds().size());
        updateMapMarkersForSelection(route);
        List<Location> waypoints = findWaypointsByIds(route.getLocationIds());
        calculateAndDrawRoute(waypoints, position);
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

                        if (routeSuggestionAdapter != null) {
                            if (routeIndexInAdapter >= 0 && routeIndexInAdapter < currentSuggestions.size()) {
                                MapRouteSuggestion suggestionToUpdate = currentSuggestions.get(routeIndexInAdapter);
                                suggestionToUpdate.setDuration(totalDurationString);
                                suggestionToUpdate.setDistance(totalDistanceString);
                                routeSuggestionAdapter.notifyItemChanged(routeIndexInAdapter);
                            }
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
        if (currentSuggestions.isEmpty()) {
            routeInfoRecyclerView.setVisibility(View.GONE);
        } else {
            routeSuggestionAdapter = new RouteSuggestionAdapter(currentSuggestions, this);
            routeInfoRecyclerView.setAdapter(routeSuggestionAdapter);
            routeInfoRecyclerView.setVisibility(View.VISIBLE);
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
            stopsTv.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_UI);
        locationManager.startLocationUpdates(this, location -> {
            if (isMapReady) {
                webView.evaluateJavascript(String.format(java.util.Locale.US, "javascript:updateUserLocationFromApp(%f, %f)",
                        location.getLatitude(), location.getLongitude()), null);
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