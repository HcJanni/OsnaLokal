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
import java.util.stream.Collectors;

public class RouteSuggestionAdapter extends RecyclerView.Adapter<RouteSuggestionAdapter.RouteViewHolder> {

    public interface OnRouteSuggestionClickListener {
        void onRouteSuggestionClick(MapRouteSuggestion routeSuggestion, int position);
    }

    private final List<MapRouteSuggestion> suggestions;
    private final OnRouteSuggestionClickListener clickListener;
    private int selectedPosition = 0; // Die erste Route ist standardmäßig ausgewählt

    public RouteSuggestionAdapter(List<MapRouteSuggestion> suggestions, OnRouteSuggestionClickListener clickListener) {
        this.suggestions = suggestions;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_route_suggestion, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        MapRouteSuggestion suggestion = suggestions.get(position);
        holder.bind(suggestion, position, selectedPosition, clickListener);
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    public void setSelectedPosition(int position) {
        int previousSelectedPosition = this.selectedPosition;
        this.selectedPosition = position;
        notifyItemChanged(previousSelectedPosition);
        notifyItemChanged(this.selectedPosition);
    }

    static class RouteViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView card;
        private final TextView locationsText, durationText, distanceText, numberText;

        public RouteViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_route_suggestion);
            locationsText = itemView.findViewById(R.id.tv_route_locations);
            durationText = itemView.findViewById(R.id.tv_route_duration);
            distanceText = itemView.findViewById(R.id.tv_route_distance);
            numberText = itemView.findViewById(R.id.tv_route_number);
        }

        public void bind(final MapRouteSuggestion suggestion, final int position, int selectedPosition, final OnRouteSuggestionClickListener listener) {
            locationsText.setText(suggestion.getLocationsString());
            durationText.setText(suggestion.getDurationString());
            distanceText.setText(suggestion.getDistanceString());
            numberText.setText(String.valueOf(position + 1));

            boolean isSelected = position == selectedPosition;

            // Passe das Aussehen basierend auf dem Selektionsstatus an
            Context ctx = itemView.getContext();
            int primaryColor = ctx.getResources().getColor(R.color.primary, ctx.getTheme());
            int greyColor = Color.LTGRAY;

            card.setStrokeColor(isSelected ? primaryColor : greyColor);
            numberText.setTextColor(isSelected ? primaryColor : greyColor);
            ((android.graphics.drawable.GradientDrawable) numberText.getBackground()).setStroke(4, isSelected ? primaryColor : greyColor);


            itemView.setOnClickListener(v -> listener.onRouteSuggestionClick(suggestion, position));
        }
    }
}
