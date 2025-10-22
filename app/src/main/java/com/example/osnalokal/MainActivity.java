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

// WICHTIG: Nur noch die Interfaces implementieren, die wir wirklich brauchen
public class MainActivity extends AppCompatActivity implements RouteAdapter.OnRouteClickListener, NewsAdapter.OnNewsClickListener {

    // --- Klassenvariablen nur noch für Routen ---
    private List<Route> allRoutes = new ArrayList<>();
    private RouteAdapter allRoutesAdapter;

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

        // 2. LADE DIE NEUEN ROUTEN-DATEN
        this.allRoutes = RoutesData.getAllRoutes();

        // 3. Initialisiere die UI-Komponenten (Aufrufe bereinigt)
        setupRoutesCarousel();
        setupNewsList();
        setupAllRoutesList();
        setupFilterLogic();
        setupFloatingActionButtons();
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
            // TODO: Diese Filterung muss noch an die Routen angepasst werden.
            // Aktuell wird bei jedem Klick die volle Liste angezeigt und gescrollt.
            allRoutesAdapter.filterList(this.allRoutes);

            // Scroll-Logik
            int targetY = listContainer.getTop() + allRoutesTitle.getTop() - ((ViewGroup.MarginLayoutParams) allRoutesTitle.getLayoutParams()).topMargin;
            int offset = 50;
            targetY += offset;
            ObjectAnimator animator = ObjectAnimator.ofInt(scrollView, "scrollY", scrollView.getScrollY(), targetY);
            animator.setDuration(400);
            animator.start();
        });
    }

    // --- Klick-Listener-Implementierungen ---

    // Dies ist jetzt der einzige Klick-Listener für die Routen/Location-Karten
    @Override
    public void onRouteClick(Route route) {
        // Wenn eine Route geklickt wird, starten wir die MapActivity
        // und übergeben die Liste der Location-IDs, die angezeigt werden sollen.
        Intent intent = new Intent(this, MapActivity.class);

        // WICHTIG: Die Liste der IDs wird als "Extra" mit dem Schlüssel "LOCATION_IDS" hinzugefügt.
        intent.putExtra("LOCATION_IDS", (Serializable) route.getLocationIds());

        startActivity(intent);
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
}
