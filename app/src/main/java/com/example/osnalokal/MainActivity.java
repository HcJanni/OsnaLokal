package com.example.osnalokal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RouteAdapter.OnRouteClickListener {

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
        RecyclerView recyclerView = findViewById(R.id.recycler_view_routen);
        List<Route> routes = new ArrayList<>();
        routes.add(new Route("Ein Tag in Osnabrück", "Frühstück, Aktivitäten und Abendessen", "4 KM", R.drawable.rec_tours_testimg));
        routes.add(new Route("Schlossgarten & Co.", "Entspannung im Grünen", "2 KM", R.drawable.rec_tours_testimg));
        routes.add(new Route("Historische Altstadt", "Eine Reise in die Vergangenheit", "5 KM", R.drawable.rec_tours_testimg));

        // 2. Adapter-Namen korrigieren und 'this' als Listener übergeben
        RouteAdapter adapter = new RouteAdapter(routes, this);

        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
    }

    // 3. Implementiere die onRouteClick-Methode, um MapActivity zu starten
    @Override
    public void onRouteClick(Route route) {
        // Erstelle einen Intent, der zur MapActivity navigieren soll
        Intent intent = new Intent(MainActivity.this, MapActivity.class);

        // Optional: Gib Daten mit, die die MapActivity braucht
        intent.putExtra("ROUTE_TITLE", route.getTitle());

        // Starte die MapActivity
        startActivity(intent);
    }
}