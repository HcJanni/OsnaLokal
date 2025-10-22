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

        // Edge-to-Edge Handling (bleibt unverändert)
        View mainView = findViewById(R.id.firststartup);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Lade den Inhalt der Onboarding-Seite (bleibt unverändert)
        FrameLayout container = findViewById(R.id.onboarding_container);
        LayoutInflater.from(this).inflate(R.layout.onboarding_page, container, true);

        // ==========================================================
        // HIER IST DIE KORREKTUR
        // ==========================================================

        // 1. Finde den Container des wiederverwendbaren Layouts
        View navigationButtonsContainer = findViewById(R.id.navigation_buttons_container);

        // 2. Finde die Buttons INNERHALB dieses Containers
        Button finishButton = navigationButtonsContainer.findViewById(R.id.reusable_button_finish);

        // 3. Steuere die Sichtbarkeit (bleibt gleich)
        finishButton.setVisibility(View.VISIBLE);

        // 4. Setze den Klick-Listener (bleibt gleich)
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
