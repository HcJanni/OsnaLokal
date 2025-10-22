package com.example.osnalokal;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.gson.Gson;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import android.webkit.JavascriptInterface;

public class MapActivity extends AppCompatActivity {

    private WebView webView;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private String geolocationOrigin;
    private GeolocationPermissions.Callback geolocationCallback;
    private final String GOOGLE_API_KEY = "AIzaSyAnOJX2k6RMbKDlkzhlPHZq8pD8cBeHh60";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        webView = findViewById(R.id.mapWebView);
        WebSettings webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setGeolocationEnabled(true); // Wichtig für die Standort-API in JS
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);

        // 1. Lade IMMER die komplette Liste aller Orte. Das ist unsere Master-Datenbank.
        List<Location> allLocations = LocationsData.getAllLocations(this);

        // 2. Prüfe, ob eine Liste von IDs mit dem Intent übergeben wurde.
        List<Integer> receivedLocationIds = (List<Integer>) getIntent().getSerializableExtra("LOCATION_IDS");
        List<Location> waypoints = new ArrayList<>();

        if (receivedLocationIds != null && !receivedLocationIds.isEmpty()) {
            // Behalte die Reihenfolge der IDs bei!
            for (Integer id : receivedLocationIds) {
                for (Location loc : allLocations) {
                    if (loc.getId() == id) {
                        waypoints.add(loc);
                        break;
                    }
                }
            }
        } else {
            // FALL B (Fallback): Wenn keine IDs übergeben wurden, zeige einfach alle Orte an.
            waypoints.addAll(allLocations);
        }

        Gson gson = new Gson();
        final String waypointsJsonString = gson.toJson(waypoints);

        // 4. Starte die Routenberechnung im Hintergrund
        calculateAndDrawRoute(waypoints);

        // 5. Übergib die PINS an die WebView, sobald die Seite geladen ist
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    // Nur die Pins der Route übergeben
                    String javascript = "javascript:loadLocationsFromApp('" + waypointsJsonString.replace("'", "\\'") + "')";
                    webView.evaluateJavascript(javascript, null);
                }
            }
        });


        // Erstelle eine Instanz der Brücken-Klasse und übergib die Daten
        WebAppInterface webAppInterface = new WebAppInterface(allLocations);
        // Füge das Interface zur WebView hinzu und gib ihm einen Namen ("Android")
        webView.addJavascriptInterface(webAppInterface, "Android");


        // 3. Setze einen WebChromeClient, der die Daten an JS übergibt,
        //    sobald die Seite FERTIG geladen ist.
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // Wenn die Seite komplett geladen ist (100%)
                if (newProgress == 100) {
                    // Nur die Pins der Route übergeben
                    String javascript = "javascript:loadLocationsFromApp('" + waypointsJsonString.replace("'", "\\'") + "')";
                    webView.evaluateJavascript(javascript, null);
                }
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        webView.loadUrl("file:///android_asset/map.html");
        requestLocationPermission();
    }

    private void calculateAndDrawRoute(List<Location> waypoints) {
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

                // --- KORREKTUR 1: Verwende die richtige LatLng-Klasse aus der services-Bibliothek ---
                com.google.maps.model.LatLng[] intermediatePoints = new com.google.maps.model.LatLng[waypoints.size() - 2];
                for (int i = 1; i < waypoints.size() - 1; i++) {
                    Location loc = waypoints.get(i);
                    intermediatePoints[i - 1] = new com.google.maps.model.LatLng(loc.getBreitengrad(), loc.getLaengengrad());
                }

                // Führe die Anfrage an die Directions API aus
                // --- KORREKTUR 2: Auch hier die richtige LatLng-Klasse verwenden ---
                DirectionsResult result = DirectionsApi.newRequest(context)
                        .origin(new com.google.maps.model.LatLng(start.getBreitengrad(), start.getLaengengrad()))
                        .destination(new com.google.maps.model.LatLng(end.getBreitengrad(), end.getLaengengrad()))
                        .waypoints(intermediatePoints)
                        .await();

                // Extrahiere die kodierte Pfad-Linie aus dem Ergebnis
                String encodedPath = result.routes[0].overviewPolyline.getEncodedPath();

                // Übergib den Pfad an das JavaScript, um ihn zu zeichnen
                // Wichtig: Muss auf dem UI-Thread passieren!
                runOnUiThread(() -> {
                    String javascript = "javascript:drawRouteFromEncodedPath('" + encodedPath + "')";
                    webView.evaluateJavascript(javascript, null);
                });

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

            if (clickedLocation != null) {
                runOnUiThread(() -> {
                    // 1. Erstelle das Bottom Sheet
                    DetailBottomSheetFragment bottomSheet = DetailBottomSheetFragment.newInstance(
                            clickedLocation.getName(),
                            "Bewertung: " + clickedLocation.getBewertungen() + "\n" + clickedLocation.getOeffnungszeiten(),
                            R.drawable.rec_tours_testimg
                    );

                    // 2. Setze einen Listener, der auf das Schließen reagiert
                    bottomSheet.setOnDismissListener(() -> {
                        // Wenn das Sheet geschlossen wird, rufe eine JS-Funktion auf,
                        // um alle Marker zurückzusetzen.
                        webView.post(() -> webView.evaluateJavascript("javascript:resetAllMarkers()", null));
                    });

                    // 3. Zeige das Bottom Sheet an
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
