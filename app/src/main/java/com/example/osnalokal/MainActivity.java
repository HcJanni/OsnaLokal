package com.example.osnalokal;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.webkit.WebView;
import android.widget.Button;
import android.content.Intent;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private Button btnZurMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // A) Prüfen, ob <Button> als MaterialButton aufgeblasen wurde
        Button plain = findViewById(R.id.plainBtn);
        Log.d("M3CHECK", "Button class = " + (plain != null ? plain.getClass().getName() : "null"));

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

        btnZurMap = findViewById(R.id.btnZurMap);

        btnZurMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hier starten wir die MapActivity
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}