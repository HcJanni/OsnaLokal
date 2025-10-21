package com.example.osnalokal;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.chip.ChipGroup;

public class FilterActivity extends AppCompatActivity {

    // Header
    private TextView tvCancel, tvClearAll;

    // Dauer
    private EditText etDauerVon, etDauerBis;

    // Sektionen (zum Ein-/Ausklappen)
    private TextView tvAktivitaeten, tvKultur, tvBudget;
    private LinearLayout aktivitatenContent, kulturContent;
    private ChipGroup chipGroupRestaurant, chipGroupBudget;

    // Bars
    private CheckBox cbBarsKneipen;

    // Footer
    private Button btnSuggestRoutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        initViews();
        setupClickListeners();
    }

    /**
     * Findet alle UI-Elemente aus dem XML-Layout und weist sie den Variablen zu.
     */
    private void initViews() {
        tvCancel = findViewById(R.id.tv_cancel);
        tvClearAll = findViewById(R.id.tv_clear_all);

        etDauerVon = findViewById(R.id.et_dauer_von);
        etDauerBis = findViewById(R.id.et_dauer_bis);

        tvAktivitaeten = findViewById(R.id.tv_aktivitaten);
        tvKultur = findViewById(R.id.tv_kultur);
        tvBudget = findViewById(R.id.tv_budget);

        aktivitatenContent = findViewById(R.id.aktivitaten_content_wrapper); // Standardmäßig sichtbar
        kulturContent = findViewById(R.id.kultur_content_wrapper); // Standardmäßig 'gone'
        chipGroupRestaurant = findViewById(R.id.chip_group_restaurant);
        chipGroupBudget = findViewById(R.id.chip_group_budget);

        cbBarsKneipen = findViewById(R.id.cb_bars_kneipen);
        btnSuggestRoutes = findViewById(R.id.btn_suggest_routes);

        // Da tvBudget einen Pfeil hat, verstecken wir die Budget-Group standardmäßig
        chipGroupBudget.setVisibility(View.GONE);
    }

    /**
     * Weist allen interaktiven Elementen ihre Klick-Funktionen zu.
     */
    private void setupClickListeners() {
        // --- Header ---
        tvCancel.setOnClickListener(v -> {
            // Activity beenden, ohne Filter anzuwenden
            setResult(RESULT_CANCELED);
            finish();
        });

        tvClearAll.setOnClickListener(v -> clearFilters());

        // --- Sektionen (Collapsible) ---
        tvAktivitaeten.setOnClickListener(v -> toggleSectionVisibility(aktivitatenContent, tvAktivitaeten));
        tvKultur.setOnClickListener(v -> toggleSectionVisibility(kulturContent, tvKultur));
        tvBudget.setOnClickListener(v -> toggleSectionVisibility(chipGroupBudget, tvBudget)); // ChipGroup direkt umschalten

        // --- Footer ---
        btnSuggestRoutes.setOnClickListener(v -> applyFilters());
    }

    /**
     * Schaltet die Sichtbarkeit einer Sektion (View) um und dreht den Pfeil (Drawable).
     *
     * @param section Die View, die ein- oder ausgeblendet werden soll (z.B. ein LinearLayout).
     * @param header  Der TextView, der als Header dient und den Pfeil enthält.
     */
    private void toggleSectionVisibility(View section, TextView header) {
        if (section.getVisibility() == View.GONE) {
            section.setVisibility(View.VISIBLE);
            // Ändert das Drawable auf "Pfeil nach oben"
            header.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up, 0); // (Du musst ic_arrow_up zu deinen Drawables hinzufügen)
        } else {
            section.setVisibility(View.GONE);
            // Ändert das Drawable auf "Pfeil nach unten"
            header.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
        }
        // Falls du kein 'ic_arrow_up' hast, kannst du den Pfeil auch einfach drehen:
        // header.animate().rotationBy(180f).setDuration(200).start();
        // ODER einfach die Zeilen mit 'setCompoundDrawablesWithIntrinsicBounds' weglassen.
    }

    /**
     * Setzt alle Filter-Eingabefelder auf ihren Standardzustand zurück.
     */
    private void clearFilters() {
        etDauerVon.setText("");
        etDauerBis.setText("");
        chipGroupRestaurant.clearCheck();
        chipGroupBudget.clearCheck();
        cbBarsKneipen.setChecked(false);
    }

    /**
     * Sammelt alle ausgewählten Filter, packt sie in ein Intent
     * und sendet sie als Ergebnis an die aufrufende Activity (MainActivity) zurück.
     */
    private void applyFilters() {

    }
}