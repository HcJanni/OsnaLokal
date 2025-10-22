package com.example.osnalokal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class MainActivity extends AppCompatActivity implements RouteAdapter.OnRouteClickListener, NewsAdapter.OnNewsClickListener {

    // --- Klassenvariablen für die Master-Daten und den einen wichtigen Adapter ---
    private List<Route> allRoutes = new ArrayList<>();
    private RouteAdapter allRoutesAdapter; // Dieser Adapter ist wichtig für die Filter

    private List<Location> allLocations = new ArrayList<>();
    private LocationAdapter allLocationsAdapter; // Adapter für die "Alle Routen"-Liste


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Onboarding-Check (unverändert)
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        if (prefs.getBoolean("isFirstRun", true)) {
            startActivity(new Intent(this, FirstStartupActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        setupEdgeToEdge();

        // 3. Alle Daten erstellen und UI-Komponenten initialisieren
        this.allRoutes = createAllRoutes(); // Füllt die Klassenvariable mit allen Routen

        this.allLocations = LocationsData.getAllLocations(this);

        setupSuggestedRoutes();
        setupNewsList();
        setupAllRoutesList();
        setupFilterChips();
        setupFloatingActionButtons();
    }

    private void setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    // --- Setup-Methoden für eine saubere onCreate ---

    private List<Route> createAllRoutes() {
        List<Route> routes = new ArrayList<>();
        //               Titel, Beschreibung, Distanz, Bild, Kategorie, isSuggested?
        routes.add(new Route("Pizza-Tour", "Die besten Pizzen", "3 KM", R.drawable.rec_tours_testimg, "Restaurants", true));
        routes.add(new Route("Pizza-Tour", "Die besten Pizzen", "3 KM", R.drawable.rec_tours_testimg, "Restaurants", false));
        routes.add(new Route("Ein Tag in Osnabrück", "Frühstück, Aktivitäten...", "4 KM", R.drawable.rec_tours_testimg, "Sehenswürdigkeiten", true));
        routes.add(new Route("Ein Tag in Osnabrück", "Frühstück, Aktivitäten...", "4 KM", R.drawable.rec_tours_testimg, "Sehenswürdigkeiten", false));
        routes.add(new Route("Cocktail-Nacht", "Die angesagtesten Bars", "1 KM", R.drawable.rec_tours_testimg, "Bars", true));
        routes.add(new Route("Cocktail-Nacht", "Die angesagtesten Bars", "1 KM", R.drawable.rec_tours_testimg, "Bars", false));
        routes.add(new Route("Burger-Meile", "Saftige Burger", "2 KM", R.drawable.rec_tours_testimg, "Restaurants", false));
        routes.add(new Route("Schlossgarten & Co.", "Entspannung im Grünen", "2 KM", R.drawable.rec_tours_testimg, "Sehenswürdigkeiten", false));
        routes.add(new Route("Biergarten-Hopping", "Kühle Drinks", "4 KM", R.drawable.rec_tours_testimg, "Bars", false));
        return routes;
    }

    private void setupSuggestedRoutes() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view_suggested_routes);
        // Filtere die Liste, um nur vorgeschlagene Routen zu bekommen
        List<Route> suggestedList = new ArrayList<>();
        for (Route route : this.allRoutes) {
            if (route.isSuggested()) {
                suggestedList.add(route);
            }
        }
        // Dieser Adapter ist nur lokal, da er sich nicht ändert
        RouteAdapter suggestedAdapter = new RouteAdapter(suggestedList, this);
        recyclerView.setAdapter(suggestedAdapter);
    }

    private void setupAllRoutesList() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view_all_routes);
        // Initialisiere den Klassen-Adapter mit der vollen Liste
        this.allRoutesAdapter = new RouteAdapter(new ArrayList<>(this.allRoutes), this);
        recyclerView.setAdapter(this.allRoutesAdapter);
        recyclerView.setNestedScrollingEnabled(false);
    }

    private void setupNewsList() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view_news);
        List<NewsItem> newsItems = new ArrayList<>();
        newsItems.add(new NewsItem("Tag der Niedersachsen", "4 km", R.drawable.rec_tours_testimg));
        newsItems.add(new NewsItem("Weihnachtsmarkt", "10 km", R.drawable.rec_tours_testimg));

        NewsAdapter newsAdapter = new NewsAdapter(newsItems, this);
        recyclerView.setAdapter(newsAdapter);
        recyclerView.setNestedScrollingEnabled(false);
    }

    private void setupFilterChips() {
        final ChipGroup chipGroup = findViewById(R.id.chip_group_filters);
        final HorizontalScrollView filterBar = findViewById(R.id.filter_scroll_view);
        final View filterClickArea = findViewById(R.id.filter_click_area);
        final NestedScrollView scrollView = findViewById(R.id.main_scroll_view);
        final View listContainer = findViewById(R.id.all_routes_section_container);
        final TextView allRoutesTitle = findViewById(R.id.title_all_routes);
        final ViewGroup parent = (ViewGroup) filterBar.getParent();
        final ViewGroup sceneRoot = findViewById(R.id.main);

        filterClickArea.setOnClickListener(view -> {

            AutoTransition transition = new AutoTransition();
            transition.setDuration(250); // Eine schnelle, knackige Dauer

            TransitionManager.beginDelayedTransition(sceneRoot, transition);

            boolean isVisible = filterBar.getVisibility() == View.VISIBLE;
            filterBar.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        });

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (allRoutesAdapter == null) return; // Sicherheitscheck

            // ... (deine komplette Filter-Logik bleibt hier unverändert) ...
            if (checkedIds.isEmpty()) {
                allRoutesAdapter.filterList(this.allRoutes);
                return;
            }
            Chip selectedChip = group.findViewById(checkedIds.get(0));
            if (selectedChip == null) {
                allRoutesAdapter.filterList(this.allRoutes);
                return;
            }
            String selectedCategory = selectedChip.getText().toString();
            if (selectedCategory.equalsIgnoreCase("Alle Routen")) {
                allRoutesAdapter.filterList(this.allRoutes);
            } else {
                List<Route> filteredRoutes = this.allRoutes.stream()
                        .filter(route -> route.getCategory().equalsIgnoreCase(selectedCategory))
                        .collect(Collectors.toList());
                allRoutesAdapter.filterList(filteredRoutes);
            }

            // --- Logik zum Scrollen (wird jetzt immer ausgeführt) ---

            // ==========================================================
            // HIER IST DIE KORREKTE BERECHNUNG
            // ==========================================================
            // Wir ziehen den oberen Margin des Titels von der Zielposition ab,
            // um den kleinen "Übersprung" zu korrigieren.
            int targetY = listContainer.getTop() + allRoutesTitle.getTop() - ((ViewGroup.MarginLayoutParams) allRoutesTitle.getLayoutParams()).topMargin;

            int offset = 50;
            targetY += offset;
            ObjectAnimator animator = ObjectAnimator.ofInt(
                    scrollView,
                    "scrollY",
                    scrollView.getScrollY(),
                    targetY
            );
            animator.setDuration(400);
            animator.start();
        });

    }

    private void setupFloatingActionButtons() {
        FloatingActionButton fabFilter = findViewById(R.id.fab_filter_filter);
        fabFilter.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, FilterActivity.class)));

        FloatingActionButton fabMap = findViewById(R.id.fab_filter_map);
        fabMap.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, MapActivity.class)));
    }


    // --- Klick-Listener-Implementierungen ---

    @Override
    public void onRouteClick(Route route) {
        DetailBottomSheetFragment.newInstance(
                route.getTitle(),
                route.getDescription(),
                route.getImageResource()
        ).show(getSupportFragmentManager(), "DetailBottomSheet");
    }

    @Override
    public void onNewsClick(NewsItem newsItem) {
        DetailBottomSheetFragment.newInstance(
                newsItem.getTitle(),
                newsItem.getDistance(),
                newsItem.getImageResource()
        ).show(getSupportFragmentManager(), "DetailBottomSheet");
    }
}
