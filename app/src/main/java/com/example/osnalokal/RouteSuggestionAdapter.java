package com.example.osnalokal;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class RouteSuggestionAdapter extends RecyclerView.Adapter<RouteSuggestionAdapter.SuggestionViewHolder> {

    public interface OnRouteSuggestionClickListener {
        void onRouteSuggestionClick(MapRouteSuggestion routeSuggestion, int position);
    }

    private final List<MapRouteSuggestion> suggestions;
    private final OnRouteSuggestionClickListener clickListener;
    private int selectedPosition = 0;

    // Konstanten für die zwei verschiedenen Ansichts-Typen
    private static final int VIEW_TYPE_WIDE = 1;
    private static final int VIEW_TYPE_SMALL = 2;

    public RouteSuggestionAdapter(List<MapRouteSuggestion> suggestions, OnRouteSuggestionClickListener clickListener) {
        this.suggestions = suggestions;
        this.clickListener = clickListener;
    }

    // Diese Methode entscheidet, welcher Layout-Typ verwendet wird
    @Override
    public int getItemViewType(int position) {
        if (suggestions.size() == 1) {
            return VIEW_TYPE_WIDE;
        }
        return VIEW_TYPE_SMALL;
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId;
        if (viewType == VIEW_TYPE_WIDE) {
            // Stelle sicher, dass diese Layout-Datei existiert
            layoutId = R.layout.item_route_suggestion_wide;
        } else {
            // Stelle sicher, dass deine kleine Karte 'item_route_suggestion.xml' heißt
            layoutId = R.layout.item_route_suggestion;
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        MapRouteSuggestion suggestion = suggestions.get(position);
        holder.bind(suggestion, position, selectedPosition, clickListener);
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    public void setSelectedPosition(int position) {
        // Wenn die neue Position die gleiche ist wie die alte, passiert nichts.
        if (selectedPosition == position) return;

        // Speichere die alte Position, um sie zu aktualisieren.
        int previousSelectedPosition = this.selectedPosition;
        // Setze die neue Position.
        this.selectedPosition = position;

        // Benachrichtige den Adapter, dass sich die alte und die neue Karte geändert haben.
        // Dies löst onBindViewHolder für beide neu aus.
        notifyItemChanged(previousSelectedPosition);
        notifyItemChanged(this.selectedPosition);
    }

    static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView card;
        private final TextView locationsText, durationText, distanceText, numberText;

        public SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            // Diese IDs müssen in beiden Layouts (`wide` und `small`) existieren, außer `numberText`
            card = itemView.findViewById(R.id.card_route_suggestion);
            locationsText = itemView.findViewById(R.id.tv_route_locations);
            durationText = itemView.findViewById(R.id.tv_route_duration);
            distanceText = itemView.findViewById(R.id.tv_route_distance);
            // Dieser Aufruf gibt für das 'wide'-Layout 'null' zurück, was jetzt behandelt wird.
            numberText = itemView.findViewById(R.id.tv_route_number);
        }

        public void bind(final MapRouteSuggestion suggestion, final int position, int selectedPosition, final OnRouteSuggestionClickListener listener) {
            locationsText.setText(suggestion.getLocationsString());
            durationText.setText(suggestion.getDurationString());
            distanceText.setText(suggestion.getDistanceString());

            boolean isSelected = position == selectedPosition;
            Context ctx = itemView.getContext();
            int primaryColor = ctx.getResources().getColor(R.color.primary, ctx.getTheme());
            // FALLBACK: Verwende eine Standard-Systemfarbe, falls 'inactive_suggestion_border' nicht existiert
            int greyColor;
            try {
                greyColor = ctx.getResources().getColor(R.color.inactive_suggestion_border, ctx.getTheme());
            } catch (Exception e) {
                greyColor = Color.LTGRAY; // Sicherer Fallback
            }

            if (card != null) {
                card.setStrokeColor(isSelected ? primaryColor : greyColor);
            }

            // Führe Code für den Nummernkreis nur aus, wenn die View existiert (d.h. nicht im 'wide'-Layout)
            if (numberText != null) {
                numberText.setText(String.valueOf(position + 1));
                numberText.setTextColor(isSelected ? primaryColor : greyColor);
                if (numberText.getBackground() instanceof android.graphics.drawable.GradientDrawable) {
                    ((android.graphics.drawable.GradientDrawable) numberText.getBackground()).setStroke(4, isSelected ? primaryColor : greyColor);
                }
            }

            itemView.setOnClickListener(v -> listener.onRouteSuggestionClick(suggestion, position));
        }
    }
}
