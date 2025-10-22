package com.example.osnalokal;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;
import java.util.List;

import android.webkit.JavascriptInterface;

public class MapActivity extends AppCompatActivity {

    private WebView webView;
    private LocationManager locationManager;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private String geolocationOrigin;
    private GeolocationPermissions.Callback geolocationCallback;
    private final String GOOGLE_API_KEY = "AIzaSyAnOJX2k6RMbKDlkzhlPHZq8pD8cBeHh60";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        locationManager = LocationManager.getInstance(this);

        // --- 1. WebView initialisieren ---
        webView = findViewById(R.id.mapWebView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setDomStorageEnabled(true);

        // --- 2. Daten laden und filtern ---
        List<Location> allLocations = LocationsData.getAllLocations(this);
        List<Integer> receivedLocationIds = (List<Integer>) getIntent().getSerializableExtra("LOCATION_IDS");
        List<Location> waypoints = new ArrayList<>();
        String routeName = getIntent().getStringExtra("ROUTE_NAME");

        TextView tvRouteTitle = findViewById(R.id.tv_route_title);
        TextView tvStopsCount = findViewById(R.id.tv_stops_count);
        ImageView iconBack = findViewById(R.id.icon_back);

        if (receivedLocationIds != null && !receivedLocationIds.isEmpty()) {
            for (Integer id : receivedLocationIds) {
                for (Location loc : allLocations) {
                    if (loc.getId() == id) {
                        waypoints.add(loc);
                        break;
                    }
                }
            }
        } else {
            waypoints.addAll(allLocations); // Fallback
        }

        // --- Toolbar Texte setzen ---
        if (routeName != null) {
            tvRouteTitle.setText(routeName);
        } else {
            tvRouteTitle.setText("Karte"); // Fallback-Titel
        }
        int stopsCount = (receivedLocationIds != null) ? receivedLocationIds.size() : 0;
        tvStopsCount.setText(stopsCount + " Stopps");

        iconBack.setOnClickListener(v -> {
            finish(); // Beendet die MapActivity und kehrt zum vorherigen Bildschirm zurück
        });

        // --- 3. JavaScript-Brücke einrichten ---
        webView.addJavascriptInterface(new WebAppInterface(waypoints), "Android");

        // --- 4. WebChromeClient (KORRIGIERT: Nur noch EIN Client) ---
        final String waypointsJsonString = new Gson().toJson(waypoints);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    // Übergib die Pins der Route (unverändert)
                    String javascriptPins = "javascript:loadLocationsFromApp('" + waypointsJsonString.replace("'", "\\'") + "')";
                    webView.evaluateJavascript(javascriptPins, null);

                    // --- NEU: SOFORTIGE STANDORT-ÜBERGABE ---
                    // Hole die letzte bekannte Position und übergib sie an eine neue JS-Funktion
                    android.location.Location lastLocation = locationManager.getLastKnownLocation();
                    if (lastLocation != null) {
                        String javascriptUserPos = "javascript:updateUserLocationFromApp(" + lastLocation.getLatitude() + ", " + lastLocation.getLongitude() + ")";
                        webView.evaluateJavascript(javascriptUserPos, null);
                    }
                }
            }

            // Die onGeolocationPermissionsShowPrompt wird nicht mehr benötigt!
            // Du kannst den Inhalt löschen, um den Code sauberer zu machen.
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                // Nicht mehr nötig, da der Standort von der nativen App kommt.
                // Man kann es als Fallback drinlassen oder den Inhalt leeren.
                callback.invoke(origin, false, false); // Sicherstellen, dass JS nicht selbst fragt
            }
        });

        // --- 5. Routenberechnung starten ---
        calculateAndDrawRoute(waypoints);

        // --- 6. WebView laden ---
        webView.loadUrl("file:///android_asset/map.html");

        // --- 7. FAB einrichten ---
        FloatingActionButton fab = findViewById(R.id.fab_center_on_user);
        fab.setOnClickListener(v -> {
            webView.evaluateJavascript("javascript:centerOnUserLocation()", null);
        });

        setupEdgeToEdge();
    }

    private void setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.map), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void calculateAndDrawRoute(List<Location> waypoints) {        // Die Route muss mindestens einen Start- und einen Endpunkt haben
        if (waypoints.size() < 2) {
            android.util.Log.d("MapActivity", "Nicht genügend Wegpunkte für eine Route.");
            return;
        }

        // Führe die Netzwerkanfrage in einem neuen Thread aus
        new Thread(() -> {
            try {
                GeoApiContext context = new GeoApiContext.Builder()
                        .apiKey(GOOGLE_API_KEY)
                        .build();

                Location start = waypoints.get(0);
                Location end = waypoints.get(waypoints.size() - 1);

                // --- KORREKTUR BEI DEN ZWISCHENPUNKTEN ---
                com.google.maps.model.LatLng[] intermediatePoints = new com.google.maps.model.LatLng[waypoints.size() - 2];
                for (int i = 1; i < waypoints.size() - 1; i++) {
                    Location loc = waypoints.get(i);
                    // Verwende Breitengrad UND Laengengrad
                    intermediatePoints[i - 1] = new com.google.maps.model.LatLng(loc.getBreitengrad(), loc.getLaengengrad());
                }

                // Führe die Anfrage an die Directions API aus
                // --- KORREKTUR BEI START- UND ENDPUNKT ---
                DirectionsResult result = DirectionsApi.newRequest(context)
                        // Verwende Breitengrad UND Laengengrad für den Start
                        .origin(new com.google.maps.model.LatLng(start.getBreitengrad(), start.getLaengengrad()))
                        // Verwende Breitengrad UND Laengengrad für das Ziel
                        .destination(new com.google.maps.model.LatLng(end.getBreitengrad(), end.getLaengengrad()))
                        .waypoints(intermediatePoints)
                        .mode(TravelMode.WALKING)
                        .await();

                // Extrahiere die kodierte Pfad-Linie aus dem Ergebnis
                if (result.routes != null && result.routes.length > 0) {
                    String encodedPath = result.routes[0].overviewPolyline.getEncodedPath();

                    // Übergib den Pfad an das JavaScript, um ihn zu zeichnen
                    runOnUiThread(() -> {
                        String escapedEncodedPath = encodedPath.replace("\\", "\\\\");

                        // Baue den JavaScript-Aufruf mit dem sauber escapeten String
                        String javascript = "javascript:drawRouteFromEncodedPath('" + escapedEncodedPath + "')";
                        webView.evaluateJavascript(javascript, null);
                    });
                }

            } catch (Exception e) {
                android.util.Log.e("MapActivity", "Fehler bei der Routenberechnung: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void requestLocationPermission() {
        // Prüfe, ob die Berechtigung bereits erteilt wurde
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Wenn nicht, frage den Nutzer nach der Berechtigung
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void drawTestRouteInWebView() {
        String startPoint = "52.2726,8.0449"; // Schloss
        String endPoint = "52.2799,8.0422";   // Dom

        // Escape der Anführungszeichen für den JavaScript-Aufruf
        String startPointJs = "'" + startPoint + "'";
        String endPointJs = "'" + endPoint + "'";
        String apiKeyJs = "'" + GOOGLE_API_KEY + "'";

        // Erstelle den JavaScript-Aufruf
        String javascript = "javascript:drawRouteFromApp(" + startPointJs + ", " + endPointJs + ", " + apiKeyJs + ")";

        // Führe den Code in der WebView aus
        webView.post(() -> webView.evaluateJavascript(javascript, null));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Der Nutzer hat die Berechtigung erteilt.
                if (geolocationCallback != null) {
                    // Gib dem JavaScript (über den gemerkten Callback) Bescheid, dass es jetzt loslegen darf.
                    geolocationCallback.invoke(geolocationOrigin, true, false);
                }
                // Lade die WebView neu, damit das JS die Standortabfrage sicher neu startet.
                webView.reload();
            } else {
                // Der Nutzer hat die Berechtigung verweigert.
                if (geolocationCallback != null) {
                    // Gib dem JavaScript Bescheid, dass es nicht darf.
                    geolocationCallback.invoke(geolocationOrigin, false, false);
                }
                Toast.makeText(this, "Standortberechtigung verweigert. Die Karte kann deine Position nicht anzeigen.", Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    public class WebAppInterface {
        private List<Location> locations;

        WebAppInterface(List<Location> locations) {
            this.locations = locations;
        }

        /**
         * Diese Methode wird von JavaScript aus aufgerufen, wenn ein Pin geklickt wird.
         * @param locationId Die ID des angeklickten Ortes.
         */
        @JavascriptInterface
        public void onMarkerClick(int locationId) {
            Location clickedLocation = findLocationById(locationId);

            if (clickedLocation != null) {runOnUiThread(() -> {
                DetailBottomSheetFragment bottomSheet = DetailBottomSheetFragment.newInstance(
                        clickedLocation.getName(),                 // title
                        clickedLocation.getArt(),                  // type
                        clickedLocation.getBeschreibung(),        // description
                        String.valueOf(clickedLocation.getBewertungen()), // rating
                        clickedLocation.getOeffnungszeiten(),      // openingTimes
                        clickedLocation.getBudgetAsEuroString(),          // budget
                        R.drawable.rec_tours_testimg               // imageRes
                );

                bottomSheet.setOnDismissListener(() -> {
                    webView.post(() -> webView.evaluateJavascript("javascript:resetAllMarkers()", null));
                });
                bottomSheet.show(getSupportFragmentManager(), "DetailBottomSheetFromMap");
            });
            }
        }

        private Location findLocationById(int id) {
            for (Location loc : locations) {
                if (loc.getId() == id) {
                    return loc;
                }
            }
            return null; // Wenn keine Location mit der ID gefunden wurde
        }
    }
}
