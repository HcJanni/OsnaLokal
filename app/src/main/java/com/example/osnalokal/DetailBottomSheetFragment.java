package com.example.osnalokal;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// import android.widget.ImageView; // Wird nicht mehr für closeButton gebraucht
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

// ### WICHTIGE IMPORTS ###
import com.bumptech.glide.Glide; // Zum Laden von Bildern
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton; // <-- KORRIGIERTER IMPORT
import com.google.android.material.imageview.ShapeableImageView;


public class DetailBottomSheetFragment extends BottomSheetDialogFragment {

    // Schlüssel für die Datenübergabe
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_TYPE = "arg_type";
    private static final String ARG_DESCRIPTION = "arg_description";
    private static final String ARG_RATING = "arg_rating";
    private static final String ARG_OPENINGTIMES = "arg_OPENINGTIMES";
    private static final String ARG_BUDGET = "arg_budget";
    private static final String ARG_IMAGE_PATH = "image_path"; // Key für den String-Pfad
    private static final String ARG_IMAGE_RES_LEGACY = "arg_image_res_legacy"; // Für alte News-Items (int)

    private OnDismissListener onDismissListener;

    // Statische Methode für LOCATIONS (nutzt String-Pfad)
    public static DetailBottomSheetFragment newInstance(String title, String description, String type, String rating, String openingtimes, String budget, String imagePfad) {
        DetailBottomSheetFragment fragment = new DetailBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESCRIPTION, description);
        args.putString(ARG_TYPE, type);
        args.putString(ARG_RATING, rating);
        args.putString(ARG_OPENINGTIMES, openingtimes);
        args.putString(ARG_BUDGET, budget);
        args.putString(ARG_IMAGE_PATH, imagePfad); // Speichert den Pfad als String
        fragment.setArguments(args);
        return fragment;
    }

    // Interface (unverändert)
    public interface OnDismissListener {
        void onDismiss();
    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Views finden
        TextView titleView = view.findViewById(R.id.bottom_sheet_title);
        TextView descriptionView = view.findViewById(R.id.bottom_sheet_description);
        ShapeableImageView imageView = view.findViewById(R.id.bottom_sheet_image);

        // --- HIER IST DIE CRASH-KORREKTUR (Zeile 94) ---
        MaterialButton closeButton = view.findViewById(R.id.bottom_sheet_close_button);
        // --- ENDE KORREKTUR ---

        if (getArguments() != null) {
            String title = getArguments().getString(ARG_TITLE);
            titleView.setText(title);

            // Prüfe, ob es eine Location ist (hat unseren NEUEN Bild-Pfad)
            if (getArguments().containsKey(ARG_IMAGE_PATH)) {
                // FALL 1: Es ist eine Location (mit String imagePfad)
                String type = getArguments().getString(ARG_TYPE, ""); // Fallback auf Leerstring
                String description = getArguments().getString(ARG_DESCRIPTION, "");
                String openingTimes = getArguments().getString(ARG_OPENINGTIMES, "k.A.");
                String rating = getArguments().getString(ARG_RATING, "-");
                String budget = getArguments().getString(ARG_BUDGET, "");
                String imagePath = getArguments().getString(ARG_IMAGE_PATH);

                String fullDescription = description + "\n\n"
                        + "Öffnungszeiten: " + openingTimes
                        + "\nBewertung: " + rating + " ★"
                        + "\nBudget: " + budget;
                descriptionView.setText(fullDescription);

                // --- BILD MIT GLIDE LADEN ---
                if (imagePath != null && !imagePath.isEmpty() && getContext() != null) {
                    Glide.with(getContext())
                            .load(imagePath)
                            .centerCrop()
                            .placeholder(R.drawable.rec_tours_testimg)
                            .error(R.drawable.rec_tours_testimg)
                            .into(imageView);
                } else {
                    imageView.setImageResource(R.drawable.rec_tours_testimg); // Not-Fallback
                }

            } else if (getArguments().containsKey(ARG_IMAGE_RES_LEGACY)) {
                // FALL 2: Es ist eine "alte" News (mit int imageRes)
                String description = getArguments().getString(ARG_DESCRIPTION);
                descriptionView.setText(description);
                int imageRes = getArguments().getInt(ARG_IMAGE_RES_LEGACY);
                imageView.setImageResource(imageRes);
            }
        }

        // Schließt das Bottom Sheet beim Klick
        closeButton.setOnClickListener(v -> dismiss());
    }
}