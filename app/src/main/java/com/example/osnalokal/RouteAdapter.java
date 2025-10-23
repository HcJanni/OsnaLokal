package com.example.osnalokal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteViewHolder> {

    public interface OnRouteClickListener {
        void onRouteClick(Route route);
    }

    private List<Route> routes;
    private final OnRouteClickListener clickListener;
    private final int layoutId;

    public RouteAdapter(List<Route> routes, OnRouteClickListener clickListener, int layoutId) {
        this.routes = routes;
        this.clickListener = clickListener;
        this.layoutId = layoutId;
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(this.layoutId, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        Route currentRoute = routes.get(position);
        holder.bind(currentRoute, clickListener); // Delegiere das Binden an den ViewHolder
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    public void filterList(List<Route> filteredList) {
        this.routes = filteredList;
        notifyDataSetChanged();
    }

    // Die ViewHolder-Klasse ist für das Halten und Binden der Views zuständig.
    static class RouteViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView description;
        Chip distanceChip;
        Chip categoryChip;
        Chip sustainabilityChip;
        Button viewRouteButton;

        public RouteViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_route);
            title = itemView.findViewById(R.id.text_route_title);
            description = itemView.findViewById(R.id.text_route_description);
            distanceChip = itemView.findViewById(R.id.chip_distance);
            categoryChip = itemView.findViewById(R.id.chip_category);
            viewRouteButton = itemView.findViewById(R.id.button_view_route);
            sustainabilityChip = itemView.findViewById(R.id.chip_sustainability);
        }

        // NEU: Eigene bind-Methode für sauberen Code
        public void bind(final Route route, final OnRouteClickListener listener) {
            // Binde die Standard-Daten
            title.setText(route.getName());
            description.setText(route.getDescription());
            image.setImageResource(route.getImageResource()); // Behält den Platzhalter für jetzt

            // ===== KORREKTUR 1: Setze den Kategorie-Text korrekt =====
            categoryChip.setText(route.getCategory());

            // Setze die Anzahl der Stationen
            distanceChip.setText(route.getLocationIds().size() + " Stationen");

            // Steuere die Sichtbarkeit des Nachhaltigkeits-Chips
            if (route.isSustainable()) {
                sustainabilityChip.setVisibility(View.VISIBLE);
            } else {
                sustainabilityChip.setVisibility(View.GONE);
            }

            // Setze den Klick-Listener auf den Button
            viewRouteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRouteClick(route);
                }
            });
        }
    }
}