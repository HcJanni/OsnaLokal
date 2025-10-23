package com.example.osnalokal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FilterActivity extends AppCompatActivity {

    // Header
    private TextView tvCancel, tvClearAll;

    // Dauer
    private LinearLayout durationWrapper; // NEU: Wrapper für die Dauer-Sektion
    private EditText etDauerVon, etDauerBis;

    // Sektionen (zum Ein-/Ausklappen und Checkboxen)
    private TextView tvRestaurant, tvBudget;
    private List<CheckBox> categoryCheckBoxes; // NEU: Liste für alle Kategorie-Checkboxen
    private CheckBox cbAktivitaeten, cbSehenswuerdigkeiten, cbBarsKneipen, cbParks;
    private ChipGroup chipGroupRestaurant, chipGroupBudget;
    private LinearLayout restaurantContent, budgetContentWrapper;

    // Footer
    private Button btnSuggestRoutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        setupEdgeToEdge();

        initViews();
        setupDurationValidation();
        setupClickListeners(); // Diese Methode wird jetzt die ganze UI-Logik steuern
        updateUiBasedOnFilters(); // Initialer UI-Zustand beim Start
    }

    private void setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.filter), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        tvCancel = findViewById(R.id.tv_cancel);
        tvClearAll = findViewById(R.id.tv_clear_all);

        durationWrapper = findViewById(R.id.dauer_wrapper); // NEU
        etDauerVon = findViewById(R.id.et_dauer_von);
        etDauerBis = findViewById(R.id.et_dauer_bis);

        cbAktivitaeten = findViewById(R.id.cb_aktivitaten);
        cbSehenswuerdigkeiten = findViewById(R.id.cb_sehenswuerdigkeiten);
        cbBarsKneipen = findViewById(R.id.cb_bars_kneipen);
        cbParks = findViewById(R.id.cb_parks);
        // NEU: Alle Checkboxen für einfachere Listener in einer Liste zusammenfassen
        categoryCheckBoxes = new ArrayList<>(Arrays.asList(cbAktivitaeten, cbSehenswuerdigkeiten, cbBarsKneipen, cbParks));

        tvRestaurant = findViewById(R.id.tv_restaurant);
        tvBudget = findViewById(R.id.tv_budget);

        restaurantContent = findViewById(R.id.restaurant_content_wrapper);
        budgetContentWrapper = findViewById(R.id.budget_content_wrapper);

        chipGroupRestaurant = findViewById(R.id.chip_group_restaurant);
        chipGroupBudget = findViewById(R.id.chip_group_budget);

        btnSuggestRoutes = findViewById(R.id.reusable_button_finish);

        restaurantContent.setVisibility(View.GONE);
        budgetContentWrapper.setVisibility(View.GONE);

        // Tags für das Budget setzen, um es später leichter auszulesen
        ((Chip) chipGroupBudget.getChildAt(0)).setTag("günstig"); // €
        ((Chip) chipGroupBudget.getChildAt(1)).setTag("mittel");  // €€
        ((Chip) chipGroupBudget.getChildAt(2)).setTag("teuer");   // €€€
    }

    private void setupDurationValidation() {
        // Diese Methode ist bereits gut und bleibt unverändert.
        final int MAX_HOURS = 12;
        final int MIN_HOURS = 1;

        TextWatcher durationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateUiBasedOnFilters(); // NEU: UI nach jeder Eingabe aktualisieren
                if (!s.toString().isEmpty()) {
                    try {
                        int value = Integer.parseInt(s.toString());
                        EditText currentEditText = getCurrentFocus() instanceof EditText ? (EditText) getCurrentFocus() : null;

                        if (currentEditText != null) {
                            if (value > MAX_HOURS) {
                                currentEditText.setError("Maximal " + MAX_HOURS + " Stunden erlaubt");
                            } else if (value < MIN_HOURS) {
                                if (value != 0) currentEditText.setError("Minimal " + MIN_HOURS + " Stunde erlaubt");
                            } else {
                                currentEditText.setError(null);
                            }
                        }
                    } catch (NumberFormatException e) { /* Ignorieren */ }
                }
            }
        };

        etDauerVon.addTextChangedListener(durationWatcher);
        etDauerBis.addTextChangedListener(durationWatcher);
    }

    /**
     * Weist allen interaktiven Elementen ihre Klick-Funktionen zu
     * und sorgt dafür, dass nach jeder Interaktion die UI aktualisiert wird.
     */
    private void setupClickListeners() {
        // Header
        tvCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
        tvClearAll.setOnClickListener(v -> {
            clearFilters();
            updateUiBasedOnFilters(); // UI nach dem Löschen aktualisieren
        });

        // Sektionen zum Ein- und Ausklappen
        tvRestaurant.setOnClickListener(v -> toggleSectionVisibility(restaurantContent, tvRestaurant));
        tvBudget.setOnClickListener(v -> toggleSectionVisibility(budgetContentWrapper, tvBudget));

        // Footer Button
        btnSuggestRoutes.setOnClickListener(v -> applyFiltersAndFinish());

        // --- NEU: Listener für alle Filter-Interaktionen ---
        // Listener für alle Kategorie-Checkboxen
        for (CheckBox cb : categoryCheckBoxes) {
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> updateUiBasedOnFilters());
        }

        // Listener für die Restaurant-Tags
        chipGroupRestaurant.setOnCheckedStateChangeListener((group, checkedIds) -> updateUiBasedOnFilters());
    }

    /**
     * NEU: Die zentrale Logik-Methode. Sie prüft den aktuellen Filter-Zustand
     * und passt die UI (Dauer-Sichtbarkeit, Button-Text, Button-Aktivität) entsprechend an.
     */
    private void updateUiBasedOnFilters() {
        FilterCriteria criteria = buildFilterCriteriaFromUI();

        if (!criteria.hasAnyCategorySelected()) {
            // Zustand 1: Nichts ausgewählt
            durationWrapper.setVisibility(View.GONE);
            btnSuggestRoutes.setText("Bitte wähle eine Kategorie");
            btnSuggestRoutes.setEnabled(false);

        } else if (criteria.isSingleCategoryMode()) {
            // Zustand 2: Nur EINE Kategorie -> "Orte entdecken"-Modus
            durationWrapper.setVisibility(View.GONE);
            btnSuggestRoutes.setText("Orte auf Karte anzeigen");
            btnSuggestRoutes.setEnabled(true);

        } else {
            // Zustand 3: MEHRERE Kategorien -> "Routen planen"-Modus
            durationWrapper.setVisibility(View.VISIBLE);
            btnSuggestRoutes.setText("Routen-Vorschläge erhalten");
            // Button nur aktiv, wenn eine Dauer eingegeben wurde
            boolean hasDuration = criteria.minDurationHours != null || criteria.maxDurationHours != null;
            btnSuggestRoutes.setEnabled(hasDuration);
        }
    }


    /**
     * Sammelt die Filter, validiert sie und sendet das Ergebnis an die MapActivity.
     */
    private void applyFiltersAndFinish() {
        FilterCriteria criteria = buildFilterCriteriaFromUI();

        // Diese Methode wird nur aufgerufen, wenn der Button aktiv ist,
        // daher sind die grundlegenden Prüfungen bereits durch updateUiBasedOnFilters() abgedeckt.
        // Ein zusätzlicher Check schadet aber nicht.
        if (!criteria.hasAnyCategorySelected()) {
            Toast.makeText(this, "Bitte wähle mindestens eine Kategorie.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!criteria.isSingleCategoryMode()) {
            boolean hasDuration = criteria.minDurationHours != null || criteria.maxDurationHours != null;
            if (!hasDuration) {
                Toast.makeText(this, "Bitte gib eine Dauer an, um Routen zu erstellen.", Toast.LENGTH_LONG).show();
                etDauerVon.requestFocus();
                return;
            }
        }

        // Ergebnis an MapActivity zurücksenden
        Intent resultIntent = new Intent();
        resultIntent.putExtra("FILTER_CRITERIA", criteria);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void toggleSectionVisibility(View section, TextView header) {
        // Diese Methode ist bereits gut und bleibt unverändert.
        if (section.getVisibility() == View.GONE) {
            section.setVisibility(View.VISIBLE);
            header.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up, 0);
        } else {
            section.setVisibility(View.GONE);
            header.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
        }
    }

    private void clearFilters() {
        // Diese Methode ist bereits gut und bleibt unverändert.
        etDauerVon.setText("");
        etDauerBis.setText("");
        chipGroupRestaurant.clearCheck();
        chipGroupBudget.clearCheck();
        for (CheckBox cb : categoryCheckBoxes) {
            cb.setChecked(false);
        }
    }

    private FilterCriteria buildFilterCriteriaFromUI() {
        // Diese Methode ist bereits gut und bleibt unverändert.
        FilterCriteria criteria = new FilterCriteria();

        String minHoursStr = etDauerVon.getText().toString();
        if (!minHoursStr.isEmpty()) criteria.minDurationHours = Integer.parseInt(minHoursStr);
        String maxHoursStr = etDauerBis.getText().toString();
        if (!maxHoursStr.isEmpty()) criteria.maxDurationHours = Integer.parseInt(maxHoursStr);

        if (cbAktivitaeten.isChecked()) {
            criteria.categories.add("Aktivitaeten"); // Ohne 'ä'
        }
        if (cbSehenswuerdigkeiten.isChecked()) {
            criteria.categories.add("Sehenswuerdigkeit"); // Ohne 'ü'
        }
        if (cbBarsKneipen.isChecked()) {
            criteria.categories.add("Bar");
        }
        if (cbParks.isChecked()) {
            criteria.categories.add("Park");
        }

        for (int i = 0; i < chipGroupRestaurant.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupRestaurant.getChildAt(i);
            if (chip.isChecked()) {
                // Wenn ein Restaurant-Chip gewählt wird, füge "Restaurant" als Kategorie hinzu
                if (!criteria.categories.contains("Restaurant")) {
                    criteria.categories.add("Restaurant");
                }
                criteria.restaurantTags.add(chip.getText().toString().toLowerCase());
            }
        }

        int selectedBudgetChipId = chipGroupBudget.getCheckedChipId();
        if (selectedBudgetChipId != View.NO_ID) {
            Chip selectedChip = findViewById(selectedBudgetChipId);
            criteria.budget = (String) selectedChip.getTag();
        }

        return criteria;
    }
}
