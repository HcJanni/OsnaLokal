package com.example.osnalokal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// 1. Tippfehler im Klassennamen korrigiert (Router -> Route)
public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteViewHolder> {

    // 2. Interface für den Klick-Mechanismus hinzugefügt
    public interface OnRouteClickListener {
        void onRouteClick(Route route);
    }

    private List<Route> routeList;
    private final OnRouteClickListener clickListener; // Variable für den Listener

    // 3. Konstruktor angepasst: Er braucht jetzt den Listener
    public RouteAdapter(List<Route> routeList, OnRouteClickListener clickListener) {
        this.routeList = routeList;
        this.clickListener = clickListener;
    }

    public void filterList(List<Route> filteredList) {
        this.routeList = filteredList;
        notifyDataSetChanged(); // Sagt dem RecyclerView, dass er sich neu zeichnen soll
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route_card, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        Route currentRoute = routeList.get(position);
        holder.title.setText(currentRoute.getTitle());
        holder.description.setText(currentRoute.getDescription());
        holder.distance.setText(currentRoute.getDistance());
        holder.image.setImageResource(currentRoute.getImageResource());
        holder.categoryTag.setText(currentRoute.getCategory());

        // 4. Klick-Listener auf den Button setzen
        holder.viewRouteButton.setOnClickListener(v -> {
            clickListener.onRouteClick(currentRoute);
        });
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

    static class RouteViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        com.google.android.material.chip.Chip distance;
        TextView title;
        TextView description;
        com.google.android.material.button.MaterialButton viewRouteButton;
        com.google.android.material.chip.Chip categoryTag;

        public RouteViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_route);
            // 5. ID korrigiert (chip_distance -> text_distance)
            distance = itemView.findViewById(R.id.chip_distance);
            title = itemView.findViewById(R.id.text_route_title);
            description = itemView.findViewById(R.id.text_route_description);
            viewRouteButton = itemView.findViewById(R.id.button_view_route);
            categoryTag = itemView.findViewById(R.id.chip_category);
        }
    }
}
