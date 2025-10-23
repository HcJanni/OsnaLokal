package com.example.osnalokal;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class ImpressumActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_impressum);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_impressum);
        // Setzt den Klick-Listener für den "Zurück"-Pfeil
        toolbar.setNavigationOnClickListener(v -> finish());
    }
}
    