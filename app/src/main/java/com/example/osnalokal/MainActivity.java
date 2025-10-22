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

public class MainActivity extends AppCompatActivity implements LocationAdapter.OnLocationClickListener, NewsAdapter.OnNewsClickListener {
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

        // 1. LADE DIE DATEN AUS DER JSON-DATEI
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
    private void setupSuggestedRoutes() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view_suggested_routes);

        // Filtere die Liste für "Vorgeschlagene"
        // TODO: Füge ein "suggested" Flag zur Location.java und locations.json hinzu.
        // Fürs Erste nehmen wir einfach die ersten 3 Einträge.
        List<Location> suggestedList = new ArrayList<>(this.allLocations.subList(0, Math.min(3, this.allLocations.size())));

        LocationAdapter suggestedAdapter = new LocationAdapter(suggestedList, this);
        recyclerView.setAdapter(suggestedAdapter);
    }

    private void setupAllRoutesList() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view_all_routes);
        // Initialisiere den Klassen-Adapter mit der vollen Liste
        this.allLocationsAdapter = new LocationAdapter(new ArrayList<>(this.allLocations), this);
        recyclerView.setAdapter(this.allLocationsAdapter);
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
            if (allLocationsAdapter == null) return;

            if (checkedIds.isEmpty()) {
                allLocationsAdapter.filterList(this.allLocations);
                return;
            }

            Chip selectedChip = group.findViewById(checkedIds.get(0));
            String selectedCategory = selectedChip.getText().toString();

            if (selectedCategory.equalsIgnoreCase("Alle Routen")) {
                allLocationsAdapter.filterList(this.allLocations);
            } else {
                // Filtere die neue allLocations-Liste nach dem Feld "art"
                List<Location> filteredLocations = this.allLocations.stream()
                        .filter(location -> location.getArt().equalsIgnoreCase(selectedCategory))
                        .collect(Collectors.toList());
                allLocationsAdapter.filterList(filteredLocations);
            }

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
    public void onNewsClick(NewsItem newsItem) {
        DetailBottomSheetFragment.newInstance(
                newsItem.getTitle(),
                newsItem.getDistance(),
                newsItem.getImageResource()
        ).show(getSupportFragmentManager(), "DetailBottomSheet");
    }

    @Override
    public void onLocationClick(Location location) {
        // Wir verwenden die neuen, reichhaltigeren Daten für das BottomSheet
        DetailBottomSheetFragment.newInstance(
                location.getName(),
                "Bewertung: " + location.getBewertungen() + "\n" + location.getOeffnungszeiten(),
                R.drawable.rec_tours_testimg // Platzhalter-Bild
        ).show(getSupportFragmentManager(), "DetailBottomSheet");
    }
}
