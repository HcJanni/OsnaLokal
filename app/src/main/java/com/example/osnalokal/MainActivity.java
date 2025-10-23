package com.example.osnalokal;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
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
import com.google.android.material.snackbar.Snackbar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import androidx.core.widget.NestedScrollView;
import androidx.transition.AutoTransition;
import androidx.transition.Fade;
import androidx.transition.TransitionManager;

import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.animation.ObjectAnimator;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements RouteAdapter.OnRouteClickListener, NewsAdapter.OnNewsClickListener {

    private List<Route> allRoutes = new ArrayList<>();
    private RouteAdapter allRoutesAdapter;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationManager = LocationManager.getInstance(this);
        requestLocationPermission();

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        if (prefs.getBoolean("isFirstRun", true)) {
            startActivity(new Intent(this, FirstStartupActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        setupEdgeToEdge();

        this.allRoutes = RoutesData.getAllRoutes();

        setupRoutesCarousel();
        setupNewsList();
        setupAllRoutesList();
        setupFilterLogic();
        setupActionButtons();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            locationManager.startLocationUpdates(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationManager.startLocationUpdates(this);
            } else {
                View rootView = findViewById(R.id.main);
                Snackbar.make(rootView, "Ohne Standortberechtigung können einige Kartenfunktionen nicht genutzt werden.", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestLocationPermission();
    }

    private void setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupRoutesCarousel() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view_suggested_routes);
        List<Route> suggestedList = new ArrayList<>(this.allRoutes.subList(0, Math.min(2, this.allRoutes.size())));
        RouteAdapter suggestedAdapter = new RouteAdapter(suggestedList, this);
        recyclerView.setAdapter(suggestedAdapter);
    }

    private void setupAllRoutesList() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view_all_routes);
        this.allRoutesAdapter = new RouteAdapter(new ArrayList<>(this.allRoutes), this);
        recyclerView.setAdapter(this.allRoutesAdapter);
        recyclerView.setNestedScrollingEnabled(false);
    }

    private void setupFilterLogic() {
        final HorizontalScrollView filterBar = findViewById(R.id.filter_scroll_view);
        final View filterClickArea = findViewById(R.id.filter_click_area);
        final ViewGroup sceneRoot = findViewById(R.id.main);

        filterClickArea.setOnClickListener(view -> {
            AutoTransition transition = new AutoTransition();
            transition.setDuration(250);
            TransitionManager.beginDelayedTransition(sceneRoot, transition);
            boolean isVisible = filterBar.getVisibility() == View.VISIBLE;
            filterBar.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        });

        final ChipGroup chipGroup = findViewById(R.id.chip_group_filters);
        final NestedScrollView scrollView = findViewById(R.id.main_scroll_view);
        final View listContainer = findViewById(R.id.all_routes_section_container);
        final TextView allRoutesTitle = findViewById(R.id.title_all_routes);

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (allRoutesAdapter == null) return;

            if (checkedIds.isEmpty()) {
                allRoutesAdapter.filterList(this.allRoutes);
                return;
            }

            Chip selectedChip = group.findViewById(checkedIds.get(0));
            String selectedCategory = selectedChip.getText().toString();

            if (selectedCategory.equalsIgnoreCase("Alle Routen")) {
                allRoutesAdapter.filterList(this.allRoutes);
            } else {
                List<Route> filteredRoutes = this.allRoutes.stream()
                        .filter(route -> route.getCategory().equalsIgnoreCase(selectedCategory))
                        .collect(Collectors.toList());
                allRoutesAdapter.filterList(filteredRoutes);
            }

            int targetY = listContainer.getTop() + allRoutesTitle.getTop() - ((ViewGroup.MarginLayoutParams) allRoutesTitle.getLayoutParams()).topMargin;
            int offset = 50;
            targetY += offset;
            ObjectAnimator animator = ObjectAnimator.ofInt(scrollView, "scrollY", scrollView.getScrollY(), targetY);
            animator.setDuration(400);
            animator.start();
        });
    }

    @Override
    public void onRouteClick(Route route) {
        Intent intent = new Intent(this, MapActivity.class);

        intent.putExtra("SINGLE_ROUTE_IDS", (Serializable) route.getLocationIds());
        intent.putExtra("ROUTE_NAME", route.getName());

        startActivity(intent);
    }

    private void setupNewsList() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view_news);
        List<NewsItem> newsItems = new ArrayList<>();

        newsItems.add(new NewsItem(
                "Tag der Niedersachsen",
                "Ein großes Fest mit vielen Attraktionen in der Innenstadt. Erfahre hier mehr über das Programm.",
                R.drawable.rec_tours_testimg
        ));
        newsItems.add(new NewsItem(
                "Historischer Weihnachtsmarkt",
                "Der Weihnachtsmarkt vor dem Rathaus und der Marienkirche öffnet wieder seine Tore.",
                R.drawable.rec_tours_testimg
        ));

        NewsAdapter newsAdapter = new NewsAdapter(newsItems, this);
        recyclerView.setAdapter(newsAdapter);
        recyclerView.setNestedScrollingEnabled(false);
    }

    private void setupActionButtons() {
        View includedLayout = findViewById(R.id.reusable_button);
        Button btnSuggest = includedLayout.findViewById(R.id.reusable_button_finish);
        btnSuggest.setText("Routen erstellen");
        btnSuggest.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, FilterActivity.class)));
    }

    @Override
    public void onNewsClick(NewsItem newsItem) {
        DetailBottomSheetFragment.newInstance(
                newsItem.getTitle(),
                newsItem.getDescription(),
                newsItem.getImageResource()
        ).show(getSupportFragmentManager(), "DetailBottomSheet");
    }
}
