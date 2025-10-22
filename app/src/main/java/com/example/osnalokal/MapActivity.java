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

        // 3. Erstelle die finale Liste der Orte, die angezeigt werden sollen.
        List<Location> locationsToShow;

        List<Integer> locationIds = (List<Integer>) getIntent().getSerializableExtra("LOCATION_IDS");

        if (receivedLocationIds != null && !receivedLocationIds.isEmpty()) {
            // FALL A: Wenn IDs übergeben wurden, filtere die 'allLocations'-Liste.
            // Wir suchen alle Orte, deren ID in der empfangenen Liste enthalten ist.
            locationsToShow = allLocations.stream()
                    .filter(location -> receivedLocationIds.contains(location.getId()))
                    .collect(Collectors.toList());
        } else {
            // FALL B (Fallback): Wenn keine IDs übergeben wurden, zeige einfach alle Orte an.
            locationsToShow = allLocations;
        }

        Gson gson = new Gson();
        final String locationsJsonString = gson.toJson(locationsToShow);

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
                    // Rufe eine neue JS-Funktion auf und übergib den JSON-String.
                    // Wir ersetzen Anführungszeichen, um Fehler im JS zu vermeiden.
                    String javascript = "javascript:loadLocationsFromApp('" + locationsJsonString.replace("'", "\\'") + "')";
                    webView.evaluateJavascript(javascript, null);

                    //Für Test Route zeichnen
                    //drawTestRouteInWebView();
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
