package com.example.osnalokal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Prüfen, ob das Onboarding gezeigt werden muss (diese Logik bleibt)
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);

        if (isFirstRun) {
            Intent intent = new Intent(this, FirstStartupActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // 2. Edge-to-Edge aktivieren (macht die App bildschirmfüllend)
        EdgeToEdge.enable(this);

        // 3. Das neue Layout für die Hauptseite setzen
        setContentView(R.layout.activity_main);

        // 4. System-Padding anwenden, damit UI-Elemente nicht unter die Statusleiste rutschen
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Theme-Attribute (M3/Expressive) auflösbar?
        TypedValue tv = new TypedValue();
        boolean hasTitleLarge = getTheme().resolveAttribute(
                com.google.android.material.R.attr.textAppearanceTitleLarge, tv, true);
        boolean hasSurfaceContainer = getTheme().resolveAttribute(
                com.google.android.material.R.attr.colorSurfaceContainer, tv, true);
        Log.d("M3CHECK", "textAppearanceTitleLarge? " + hasTitleLarge);
        Log.d("M3CHECK", "colorSurfaceContainer? " + hasSurfaceContainer);

        // B) Sichttest: M3/Expressive-Dialog
        new MaterialAlertDialogBuilder(this)
                .setTitle("M3 Dialog")
                .setMessage("Sieht modern aus?")
                .setPositiveButton("OK", null)
                .show();
    }
}