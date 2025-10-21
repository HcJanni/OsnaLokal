package com.example.osnalokal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Prüfen, ob das Onboarding gezeigt werden muss (diese Logik bleibt)
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);

        if (isFirstRun) {
            Intent intent = new Intent(this, FirstStartupActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // 2. Edge-to-Edge aktivieren (macht die App bildschirmfüllend)
        EdgeToEdge.enable(this);

        // 3. Das neue Layout für die Hauptseite setzen
        setContentView(R.layout.activity_main);

        // 4. System-Padding anwenden, damit UI-Elemente nicht unter die Statusleiste rutschen
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Theme-Attribute (M3/Expressive) auflösbar?
        TypedValue tv = new TypedValue();
        boolean hasTitleLarge = getTheme().resolveAttribute(
                com.google.android.material.R.attr.textAppearanceTitleLarge, tv, true);
        boolean hasSurfaceContainer = getTheme().resolveAttribute(
                com.google.android.material.R.attr.colorSurfaceContainer, tv, true);
        Log.d("M3CHECK", "textAppearanceTitleLarge? " + hasTitleLarge);
        Log.d("M3CHECK", "colorSurfaceContainer? " + hasSurfaceContainer);

        setupRoutesCarousel();
    }

    private void setupRoutesCarousel() {
        // 1. RecyclerView im Layout finden
        RecyclerView recyclerView = findViewById(R.id.recycler_view_routen);

        // 2. Beispieldaten erstellen
        // WICHTIG: Füge Bilder mit diesen Namen (z.B. os_markt, os_schloss)
        // zu deinem 'drawable'-Ordner hinzu!
        List<Route> routes = new ArrayList<>();
        routes.add(new Route("Ein Tag in Osnabrück", "Frühstück, Aktivitäten und Abendessen", "4 KM", R.drawable.rec_tours_testimg));
        routes.add(new Route("Schlossgarten & Co.", "Entspannung im Grünen", "2 KM", R.drawable.rec_tours_testimg));
        routes.add(new Route("Historische Altstadt", "Eine Reise in die Vergangenheit", "5 KM", R.drawable.rec_tours_testimg));

        // 3. Adapter erstellen und mit den Daten füttern
        RouterAdapter adapter = new RouterAdapter(routes);

        // 4. Adapter dem RecyclerView zuweisen
        recyclerView.setAdapter(adapter);

        // Optional aber empfohlen: Performance-Optimierung
        recyclerView.setHasFixedSize(true);
    }
}