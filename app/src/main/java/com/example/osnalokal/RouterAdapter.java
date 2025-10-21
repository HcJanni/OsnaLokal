package com.example.osnalokal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class RouterAdapter extends RecyclerView.Adapter<RouterAdapter.RouteViewHolder> {

    private final List<Route> routeList;

    // Konstruktor: Der Adapter erhält eine Liste von Routen, die er anzeigen soll
    public RouterAdapter(List<Route> routeList) {
        this.routeList = routeList;
    }

    // Erstellt für jede Karte eine neue "View-Hülle" (ViewHolder)
    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route_card, parent, false);
        return new RouteViewHolder(view);
    }

    // Befüllt eine einzelne Karte mit den Daten an einer bestimmten Position
    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        Route route = routeList.get(position);
        holder.title.setText(route.getTitle());
        holder.description.setText(route.getDescription());
        holder.distance.setText(route.getDistance());
        holder.image.setImageResource(route.getImageResource()); // Setzt das Bild
    }

    // Gibt an, wie viele Elemente in der Liste sind
    @Override
    public int getItemCount() {
        return routeList.size();
    }

    // Hält die Referenzen auf die UI-Elemente einer einzelnen Karte
    static class RouteViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        com.google.android.material.chip.Chip distance;
        TextView title;
        TextView description;
        com.google.android.material.button.MaterialButton viewRouteButton;

        public RouteViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_route);
            distance = itemView.findViewById(R.id.chip_distance);
            title = itemView.findViewById(R.id.text_route_title);
            description = itemView.findViewById(R.id.text_route_description);

            // 2. Den Button anhand seiner ID im Layout finden
            viewRouteButton = itemView.findViewById(R.id.button_view_route);
        }
    }
}
