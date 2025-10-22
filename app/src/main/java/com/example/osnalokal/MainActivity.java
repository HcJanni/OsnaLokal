package com.example.osnalokal;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors; // Neuer, moderner Import für's Filtern

import androidx.core.widget.NestedScrollView;
import androidx.transition.AutoTransition;
import androidx.transition.Fade;
import androidx.transition.TransitionManager;

import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.animation.ObjectAnimator;
import android.widget.Toast;

// WICHTIG: Nur noch die Interfaces implementieren, die wir wirklich brauchen
public class MainActivity extends AppCompatActivity implements RouteAdapter.OnRouteClickListener, NewsAdapter.OnNewsClickListener {

    // --- Klassenvariablen nur noch für Routen ---
    private List<Route> allRoutes = new ArrayList<>();
    private RouteAdapter allRoutesAdapter;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialisiere den LocationManager als Singleton
        locationManager = LocationManager.getInstance(this);
        // Frage nach der Berechtigung, falls noch nicht vorhanden
        requestLocationPermission();

        // 1. Onboarding-Check (unverändert)
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        if (prefs.getBoolean("isFirstRun", true)) {
            startActivity(new Intent(this, FirstStartupActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        setupEdgeToEdge();

        // 2. LADE DIE NEUEN ROUTEN-DATEN
        this.allRoutes = RoutesData.getAllRoutes();

        // 3. Initialisiere die UI-Komponenten (Aufrufe bereinigt)
        setupRoutesCarousel();
        setupNewsList();
        setupAllRoutesList();
        setupFilterLogic();
        setupFloatingActionButtons();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Wenn keine Berechtigung, frage den Nutzer
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Wenn Berechtigung da ist, starte die Hintergrund-Updates
            locationManager.startLocationUpdates(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Nutzer hat zugestimmt, starte Hintergrund-Updates
                locationManager.startLocationUpdates(this);
            } else {
                Toast.makeText(this, "Ohne Standortberechtigung können einige Kartenfunktionen nicht genutzt werden.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Optional, aber guter Stil: Updates stoppen, wenn die App nicht sichtbar ist
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume
                ();requestLocationPermission(); // Starte Updates wieder, wenn App in den Vordergrund kommt
    }


        private void setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Passt das Karussell an, um Routen anzuzeigen
    private void setupRoutesCarousel() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view_suggested_routes);
        // Wir nehmen einfach die ersten 1-2 Routen als "vorgeschlagen"
        List<Route> suggestedList = new ArrayList<>(this.allRoutes.subList(0, Math.min(2, this.allRoutes.size())));
        // Verwende den neuen RouteAdapter
        RouteAdapter suggestedAdapter = new RouteAdapter(suggestedList, this);
        recyclerView.setAdapter(suggestedAdapter);
    }

    // Zeigt ALLE Routen in der vertikalen Liste an
    private void setupAllRoutesList() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view_all_routes);
        this.allRoutesAdapter = new RouteAdapter(new ArrayList<>(this.allRoutes), this);
        recyclerView.setAdapter(this.allRoutesAdapter);
        recyclerView.setNestedScrollingEnabled(false);
    }

    // Die Methode für die Filter-Logik (umbenannt für Klarheit)
    private void setupFilterLogic() {
        final HorizontalScrollView filterBar = findViewById(R.id.filter_scroll_view);
        final View filterClickArea = findViewById(R.id.filter_click_area);
        final ViewGroup sceneRoot = findViewById(R.id.main);

        // Logik zum Ein- und Ausblenden der Filterleiste
        filterClickArea.setOnClickListener(view -> {
            AutoTransition transition = new AutoTransition();
            transition.setDuration(250);
            TransitionManager.beginDelayedTransition(sceneRoot, transition);
            boolean isVisible = filterBar.getVisibility() == View.VISIBLE;
            filterBar.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        });

        // Logik für die Chips selbst (Filterung und Scrollen)
        final ChipGroup chipGroup = findViewById(R.id.chip_group_filters);
        final NestedScrollView scrollView = findViewById(R.id.main_scroll_view);
        final View listContainer = findViewById(R.id.all_routes_section_container);
        final TextView allRoutesTitle = findViewById(R.id.title_all_routes);

        // KORRIGIERTER LISTENER: Es gibt nur EINEN Listener
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (allRoutesAdapter == null) return;

            // Wenn kein Chip ausgewählt ist, zeige alle Routen
            if (checkedIds.isEmpty()) {
                allRoutesAdapter.filterList(this.allRoutes);
                return;
            }

            // Finde den Text des ausgewählten Chips
            Chip selectedChip = group.findViewById(checkedIds.get(0));
            String selectedCategory = selectedChip.getText().toString();

            // "Alle Routen" ist ein Sonderfall
            if (selectedCategory.equalsIgnoreCase("Alle Routen")) {
                allRoutesAdapter.filterList(this.allRoutes);
            } else {
                // Filtere die Routenliste basierend auf der neuen 'category'-Eigenschaft
                List<Route> filteredRoutes = this.allRoutes.stream()
                        .filter(route -> route.getCategory().equalsIgnoreCase(selectedCategory))
                        .collect(Collectors.toList());
                allRoutesAdapter.filterList(filteredRoutes);
            }

            // Scroll-Logik
            int targetY = listContainer.getTop() + allRoutesTitle.getTop() - ((ViewGroup.MarginLayoutParams) allRoutesTitle.getLayoutParams()).topMargin;
            int offset = 50;
            targetY += offset;
            ObjectAnimator animator = ObjectAnimator.ofInt(scrollView, "scrollY", scrollView.getScrollY(), targetY);
            animator.setDuration(400);
            animator.start();
        });
    }

    // Dies ist jetzt der einzige Klick-Listener für die Routen/Location-Karten
    @Override
    public void onRouteClick(Route route) {
        Intent intent = new Intent(this, MapActivity.class);

        //1. Die IDs der Orte (hast du schon)
        intent.putExtra("LOCATION_IDS", (Serializable) route.getLocationIds());

        // --- 2. HIER DEN NAMEN DER ROUTE HINZUFÜGEN ---
        intent.putExtra("ROUTE_NAME", route.getName());

        startActivity(intent);
    }

    private void setupNewsList() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view_news);
        List<NewsItem> newsItems = new ArrayList<>();

        // --- HIER DIE DATEN ANPASSEN ---
        newsItems.add(new NewsItem(
                "Tag der Niedersachsen",
                "Ein großes Fest mit vielen Attraktionen in der Innenstadt. Erfahre hier mehr über das Programm.", // Beschreibung statt "4 km"
                R.drawable.rec_tours_testimg
        ));
        newsItems.add(new NewsItem(
                "Historischer Weihnachtsmarkt",
                "Der Weihnachtsmarkt vor dem Rathaus und der Marienkirche öffnet wieder seine Tore.", // Beschreibung statt "10 km"
                R.drawable.rec_tours_testimg
        ));

        NewsAdapter newsAdapter = new NewsAdapter(newsItems, this);
        recyclerView.setAdapter(newsAdapter);
        recyclerView.setNestedScrollingEnabled(false);
    }

    private void setupFloatingActionButtons() {
        FloatingActionButton fabFilter = findViewById(R.id.fab_filter_filter);
        fabFilter.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, FilterActivity.class)));

        FloatingActionButton fabMap = findViewById(R.id.fab_filter_map);
        fabMap.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, MapActivity.class)));
    }


    // --- Klick-Listener-Implementierungen ---

    @Override
    public void onNewsClick(NewsItem newsItem) {
        // --- HIER DIE DATEN KORREKT ÜBERGEBEN ---
        DetailBottomSheetFragment.newInstance(
                newsItem.getTitle(),
                newsItem.getDescription(),
                newsItem.getImageResource()
        ).show(getSupportFragmentManager(), "DetailBottomSheet");
    }
}
