package com.example.osnalokal;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.InputStream;

public class DetailBottomSheetFragment extends BottomSheetDialogFragment {

    // Schlüssel für die Datenübergabe
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_TYPE = "arg_type";
    private static final String ARG_DESCRIPTION = "arg_description";
    private static final String ARG_RATING = "arg_rating";
    private static final String ARG_OPENINGTIMES = "arg_OPENINGTIMES";
    private static final String ARG_BUDGET = "arg_budget";
    private static final String ARG_IMAGE_RES = "arg_image_res";
    private static final String ARG_IMAGE_PATH = "arg_image_path"; // NEU: für den Bildpfad als String

    // Statische Methode, um eine neue Instanz zu erstellen und Daten sicher zu übergeben
    public static DetailBottomSheetFragment newInstance(String title, String description, String type, String rating, String openingtimes, String budget, int imageRes) {
        DetailBottomSheetFragment fragment = new DetailBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_TYPE, type);
        args.putString(ARG_DESCRIPTION, description);
        args.putString(ARG_OPENINGTIMES, openingtimes);
        args.putString(ARG_RATING, rating);
        args.putString(ARG_BUDGET, budget);
        args.putInt(ARG_IMAGE_RES, imageRes);
        fragment.setArguments(args);
        return fragment;
    }

    public static DetailBottomSheetFragment newInstance(String title, String description, int imageRes) {
        DetailBottomSheetFragment fragment = new DetailBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESCRIPTION, description);
        args.putInt(ARG_IMAGE_RES, imageRes);
        fragment.setArguments(args);
        return fragment;
    }

    public static DetailBottomSheetFragment newInstance(Location location) {
        DetailBottomSheetFragment fragment = new DetailBottomSheetFragment();
        Bundle args = new Bundle();

        args.putString(ARG_TITLE, location.getName());
        args.putString(ARG_DESCRIPTION, location.getBeschreibung());
        args.putString(ARG_TYPE, location.getArt());
        args.putString(ARG_RATING, String.valueOf(location.getBewertungen()));
        args.putString(ARG_OPENINGTIMES, location.getOeffnungszeiten());
        args.putString(ARG_BUDGET, location.getBudgetAsEuroString());

        // Wir holen einfach den fertigen Pfad aus dem Location-Objekt.
        String bildPfadAusJson = location.getBild();

        if (bildPfadAusJson != null && !bildPfadAusJson.isEmpty()) {
            // Wir müssen den "file:///android_asset/"-Teil entfernen,
            // da getAssets().open() nur den relativen Pfad ab dem Assets-Ordner erwartet.
            String assetPath = bildPfadAusJson.replace("file:///android_asset/", "");
            args.putString(ARG_IMAGE_PATH, assetPath);
        }

        fragment.setArguments(args);
        return fragment;
    }

    // 1. Definiere das Interface, das die Activity implementieren kann
    public interface OnDismissListener {
        void onBottomSheetDismissed();
    }

    private OnDismissListener dismissListener;

    // 2. Methode, damit die Activity den Listener setzen kann
    public void setOnDismissListener(OnDismissListener listener) {
        this.dismissListener = listener;
    }

    // 3. Diese Methode wird automatisch aufgerufen, wenn das Fragment geschlossen wird
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        // Benachrichtige den Listener, falls einer gesetzt ist
        if (dismissListener != null) {
            dismissListener.onBottomSheetDismissed();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Layout für das Bottom Sheet laden
        return inflater.inflate(R.layout.bottom_sheet_detail, container, false);
    }

    // In DetailBottomSheetFragment.java

    // In DetailBottomSheetFragment.java

    // In DetailBottomSheetFragment.java

    // In DetailBottomSheetFragment.java

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ===== KORREKTUR HIER =====
        // Die Deklarationen der Views müssen sauber und getrennt sein.

        // 1. Finde alle Views aus deinem Layout
        ShapeableImageView imageView = view.findViewById(R.id.bottom_sheet_image); // Diese Zeile war fehlerhaft
        TextView titleView = view.findViewById(R.id.bottom_sheet_title);
        TextView descriptionView = view.findViewById(R.id.bottom_sheet_description);
        Button closeButton = view.findViewById(R.id.bottom_sheet_close_button);
        TextView typeView = view.findViewById(R.id.bottom_sheet_type);

        // 2. Prüfe EINMAL, ob es überhaupt Argumente gibt.
        if (getArguments() == null) {
            // Keine Daten -> schließe das Sheet oder zeige Fehler an.
            dismiss();
            return;
        }

        // 3. Setze IMMER den Titel
        titleView.setText(getArguments().getString(ARG_TITLE));

        // 4. BILD-LOGIK: Entscheide, welches Bild geladen werden soll.
        if (getArguments().containsKey(ARG_IMAGE_PATH)) {
            // FALL 1A: Es ist eine Location mit dynamischem Bildpfad.
            String imagePath = getArguments().getString(ARG_IMAGE_PATH);
            try {
                InputStream ims = requireContext().getAssets().open(imagePath);
                Drawable d = Drawable.createFromStream(ims, null);
                imageView.setImageDrawable(d);
                ims.close();
            } catch (Exception e) {
                Log.e("BottomSheet", "Fehler beim Laden des Bildes aus Assets: " + imagePath, e);
                imageView.setImageResource(R.drawable.rec_tours_testimg); // Fallback
            }
        } else if (getArguments().containsKey(ARG_IMAGE_RES)) {
            // FALL 1B: Es ist eine News mit statischer Resource.
            imageView.setImageResource(getArguments().getInt(ARG_IMAGE_RES));
        } else {
            // FALL 1C: Weder Pfad noch Resource -> Zeige den Platzhalter.
            imageView.setImageResource(R.drawable.rec_tours_testimg);
        }

        // 5. DETAIL-LOGIK: Entscheide, welche Text-Details angezeigt werden sollen.
        if (getArguments().containsKey(ARG_TYPE)) {
            // FALL 2A: Es ist eine Location, zeige alle Details.
            String type = getArguments().getString(ARG_TYPE);
            String description = getArguments().getString(ARG_DESCRIPTION);
            String openingTimes = getArguments().getString(ARG_OPENINGTIMES);
            String rating = getArguments().getString(ARG_RATING);
            String budget = getArguments().getString(ARG_BUDGET);

            if (typeView != null) {
                typeView.setText(type);
                typeView.setVisibility(View.VISIBLE);
            }

            // Baue den Beschreibungstext für Locations zusammen
            String fullDescription = description + "\n\n"
                    + "Öffnungszeiten: " + openingTimes
                    + "\nBewertung: " + rating + " ★"
                    + "\nBudget: " + budget;
            descriptionView.setText(fullDescription);

        } else {
            // FALL 2B: Es ist eine News, zeige nur die Beschreibung.
            descriptionView.setText(getArguments().getString(ARG_DESCRIPTION));
            if (typeView != null) {
                typeView.setVisibility(View.GONE);
            }
        }

        // 6. Setze den Close-Button Listener
        closeButton.setOnClickListener(v -> dismiss());
    }
}
