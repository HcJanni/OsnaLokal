package com.example.osnalokal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class FirstStartupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firststartup);

        View mainView = findViewById(R.id.firststartup);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button finishButton = findViewById(R.id.buttonFinish);
        FrameLayout container = findViewById(R.id.onboarding_container);

        // --- HIER LADEN WIR MANUELL DIE EINE SEITE ---
        OnboardingAdapter adapter = new OnboardingAdapter();
        // Erzeuge einen ViewHolder, der das Layout der Seite enthält
        OnboardingAdapter.OnboardingViewHolder viewHolder = adapter.onCreateViewHolder(container, 0);
        // Binde die Daten (Titel, Text) an die Ansicht
        adapter.onBindViewHolder(viewHolder, 0);
        // Füge die fertige Ansicht dem Container hinzu
        container.addView(viewHolder.itemView);
        // -------------------------------------------

        // Klick-Listener für den "Fertig"-Button
        finishButton.setOnClickListener(v -> {
            markOnboardingAsFinished();
            Intent intent = new Intent(FirstStartupActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void markOnboardingAsFinished() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isFirstRun", false);
        editor.apply();
    }
}
