package com.example.osnalokal;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.ChipGroup;

public class FilterActivity extends AppCompatActivity {

    // Header
    private TextView tvCancel, tvClearAll;

    // Dauer
    private EditText etDauerVon, etDauerBis;

    // Sektionen (zum Ein-/Ausklappen und Checkboxen)
    private TextView tvRestaurant, tvBudget;
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
        setupClickListeners();
    }

    private void setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.filter), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Findet alle UI-Elemente aus dem XML-Layout und weist sie den Variablen zu.
     */
    private void initViews() {
        tvCancel = findViewById(R.id.tv_cancel);
        tvClearAll = findViewById(R.id.tv_clear_all);

        etDauerVon = findViewById(R.id.et_dauer_von);
        etDauerBis = findViewById(R.id.et_dauer_bis);

        cbAktivitaeten = findViewById(R.id.cb_aktivitaten);
        cbSehenswuerdigkeiten = findViewById(R.id.cb_sehenswuerdigkeiten);
        cbBarsKneipen = findViewById(R.id.cb_bars_kneipen);
        cbParks = findViewById(R.id.cb_parks);

        tvRestaurant = findViewById(R.id.tv_restaurant);
        tvBudget = findViewById(R.id.tv_budget);

        restaurantContent = findViewById(R.id.restaurant_content_wrapper);
        budgetContentWrapper = findViewById(R.id.budget_content_wrapper);

        chipGroupRestaurant = findViewById(R.id.chip_group_restaurant);
        chipGroupBudget = findViewById(R.id.chip_group_budget);

        btnSuggestRoutes = findViewById(R.id.reusable_button_next);

        // Standardmäßig einklappen
        restaurantContent.setVisibility(View.GONE);
        budgetContentWrapper.setVisibility(View.GONE);
    }

    private void setupDurationValidation() {
        // Definiere die Grenzwerte für die Dauer
        final int MAX_HOURS = 12;
        final int MIN_HOURS = 1;

        TextWatcher durationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    try {
                        int value = Integer.parseInt(s.toString());
                        EditText currentEditText = getCurrentFocus() instanceof EditText ? (EditText) getCurrentFocus() : null;

                        if (currentEditText != null) {
                            if (value > MAX_HOURS) {
                                currentEditText.setError("Maximal " + MAX_HOURS + " Stunden erlaubt");
                            } else if (value < MIN_HOURS) {
                                // Erlaube die Eingabe von "0", aber markiere es nicht als Fehler,
                                // da der Nutzer vielleicht "01" oder "05" tippen will.
                                if (value != 0) {
                                    currentEditText.setError("Minimal " + MIN_HOURS + " Stunde erlaubt");
                                }
                            } else {
                                currentEditText.setError(null); // Wert ist gültig
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Ignoriere den Fehler, falls die Eingabe temporär ungültig ist (z.B. leer)
                    }
                }
            }
        };

        etDauerVon.addTextChangedListener(durationWatcher);
        etDauerBis.addTextChangedListener(durationWatcher);
    }

    /**
     * Weist allen interaktiven Elementen ihre Klick-Funktionen zu.
     */
    private void setupClickListeners() {
        // Header
        tvCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        tvClearAll.setOnClickListener(v -> clearFilters());

        // Sektionen
        tvRestaurant.setOnClickListener(v -> toggleSectionVisibility(restaurantContent, tvRestaurant));
        tvBudget.setOnClickListener(v -> toggleSectionVisibility(budgetContentWrapper, tvBudget));

        // Footer
        btnSuggestRoutes.setOnClickListener(v -> applyFilters());
    }

    /**
     * Schaltet die Sichtbarkeit einer Sektion (View) um und dreht den Pfeil (Drawable).
     */
    private void toggleSectionVisibility(View section, TextView header) {
        if (section.getVisibility() == View.GONE) {
            section.setVisibility(View.VISIBLE);
            header.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up, 0);
        } else {
            section.setVisibility(View.GONE);
            header.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
        }
    }

    /**
     * Setzt alle Filter-Eingabefelder auf ihren Standardzustand zurück.
     */
    private void clearFilters() {
        etDauerVon.setText("");
        etDauerBis.setText("");
        cbAktivitaeten.setChecked(false);
        chipGroupRestaurant.clearCheck();
        chipGroupBudget.clearCheck();
        cbAktivitaeten.setChecked(false);
        cbSehenswuerdigkeiten.setChecked(false);
        cbBarsKneipen.setChecked(false);
        cbParks.setChecked(false);
    }

    /**
     * Sammelt alle ausgewählten Filter und sendet sie zurück.
     */
    private void applyFilters() {
        // Hier kommt deine Logik zum Anwenden der Filter hin.
    }
}
