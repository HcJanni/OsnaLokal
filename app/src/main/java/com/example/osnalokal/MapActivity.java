package com.example.osnalokal;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MapActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        webView = findViewById(R.id.mapWebView);

        // WebSettings holen
        WebSettings webSettings = webView.getSettings();

        // 1. JavaScript aktivieren (SEHR WICHTIG für Leaflet)
        webSettings.setJavaScriptEnabled(true);

        // 2. DOM Storage aktivieren (wird oft von Karten-Libs gebraucht)
        webSettings.setDomStorageEnabled(true);

        // 3. Zugriff auf lokale Dateien erlauben (DAMIT WIR map.html LADEN KÖNNEN)
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);

        // Verhindert, dass Links im Systembrowser geöffnet werden
        webView.setWebViewClient(new WebViewClient());

        // 4. Unsere lokale map.html laden
        // "file:///android_asset/" ist der Pfad zu deinem assets-Ordner
        webView.loadUrl("file:///android_asset/map.html");
    }
}