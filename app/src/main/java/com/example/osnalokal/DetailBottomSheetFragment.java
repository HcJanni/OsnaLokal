package com.example.osnalokal;

import android.content.DialogInterface;
import android.os.Bundle;
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

public class DetailBottomSheetFragment extends BottomSheetDialogFragment {

    // Schlüssel für die Datenübergabe
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_TYPE = "arg_type";
    private static final String ARG_DESCRIPTION = "arg_description";
    private static final String ARG_RATING = "arg_rating";
    private static final String ARG_OPENINGTIMES = "arg_OPENINGTIMES";
    private static final String ARG_BUDGET = "arg_budget";
    private static final String ARG_IMAGE_RES = "arg_image_res";

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Finde die Views aus deinem Layout
        ShapeableImageView imageView = view.findViewById(R.id.bottom_sheet_image);
        TextView titleView = view.findViewById(R.id.bottom_sheet_title);
        TextView descriptionView = view.findViewById(R.id.bottom_sheet_description);
        Button closeButton = view.findViewById(R.id.bottom_sheet_close_button);

        if (getArguments() != null) {
            String title = getArguments().getString(ARG_TITLE);
            int imageRes = getArguments().getInt(ARG_IMAGE_RES);

            titleView.setText(title);
            imageView.setImageResource(imageRes);

            // Prüfe, ob es eine Location ist (hat type, rating etc.)
            if (getArguments().containsKey(ARG_TYPE)) {
                // FALL 1: Es ist eine Location
                String type = getArguments().getString(ARG_TYPE);
                String description = getArguments().getString(ARG_DESCRIPTION);
                String openingTimes = getArguments().getString(ARG_OPENINGTIMES);
                String rating = getArguments().getString(ARG_RATING);
                String budget = getArguments().getString(ARG_BUDGET);

                // Baue den Beschreibungstext KORREKT zusammen
                String fullDescription = description + "\n\n" // <- DIE BESCHREIBUNG ANZEIGEN
                        + "Öffnungszeiten: " + openingTimes
                        + "\nBewertung: " + rating + " ★"
                        + "\nBudget: " + budget;
                descriptionView.setText(fullDescription);

            } else {
                // FALL 2: Es ist eine News
                String description = getArguments().getString(ARG_DESCRIPTION);
                descriptionView.setText(description);
            }
        }

        // Schließt das Bottom Sheet beim Klick
        closeButton.setOnClickListener(v -> dismiss());
    }
}
