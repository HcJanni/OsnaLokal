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

public class DetailBottomSheetFragment extends BottomSheetDialogFragment {

    // Schlüssel für die Datenübergabe
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_DESCRIPTION = "arg_description";
    private static final String ARG_IMAGE_RES = "arg_image_res";

    // Statische Methode, um eine neue Instanz zu erstellen und Daten sicher zu übergeben
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

        // UI-Elemente finden
        ImageView imageView = view.findViewById(R.id.bottom_sheet_image);
        TextView titleView = view.findViewById(R.id.bottom_sheet_title);
        TextView descriptionView = view.findViewById(R.id.bottom_sheet_description);
        Button closeButton = view.findViewById(R.id.bottom_sheet_close_button);

        // Daten aus den Arguments auslesen
        if (getArguments() != null) {
            titleView.setText(getArguments().getString(ARG_TITLE));
            descriptionView.setText(getArguments().getString(ARG_DESCRIPTION));
            imageView.setImageResource(getArguments().getInt(ARG_IMAGE_RES));
        }

        // Klick-Listener für den Schließen-Button
        closeButton.setOnClickListener(v -> dismiss()); // 'dismiss()' schließt das Bottom Sheet
    }
}
