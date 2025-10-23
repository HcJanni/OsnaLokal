package com.example.osnalokal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteViewHolder> {

    public interface OnRouteClickListener {
        void onRouteClick(Route route);
    }

    private List<Route> routeList;
    private final OnRouteClickListener clickListener;

    public RouteAdapter(List<Route> routeList, OnRouteClickListener clickListener) {
        this.routeList = routeList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Wir verwenden weiterhin das 'item_route_card.xml' Layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route_card, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        Route currentRoute = routeList.get(position);

        // Binde die Daten des Route-Objekts an die Views der Karte
        holder.title.setText(currentRoute.getName());
        holder.description.setText(currentRoute.getDescription());
        String imagePath = currentRoute.getImagePfad();

        Glide.with(holder.itemView.getContext())
                .load("file:///android_asset/Pictures/default.png") // Pfad zu den Assets
                .centerCrop()
                .into(holder.image);
        // Wir können die kleinen Chips für andere Infos nutzen, z.B. die Anzahl der Stationen
        holder.categoryChip.setText("Route");
        holder.distanceChip.setText(currentRoute.getLocationIds().size() + " Stationen");

        // Lese den Wert aus dem Route-Objekt
        boolean isSustainable = currentRoute.isSustainable();
        if (isSustainable) {
            holder.sustainabilityChip.setVisibility(View.VISIBLE);
        } else {
            holder.sustainabilityChip.setVisibility(View.GONE);
        }

        // Mache die ganze Karte klickbar
        holder.viewLocationButton.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onRouteClick(currentRoute);
            }
        });
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

    // Die Filter-Methode, falls du später Routen filtern willst
    public void filterList(List<Route> filteredList) {
        this.routeList = filteredList;
        notifyDataSetChanged();
    }

    static class RouteViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView description;
        Chip distanceChip;
        Chip categoryChip;
        Chip sustainabilityChip;
        Button viewLocationButton;

        public RouteViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_route);
            title = itemView.findViewById(R.id.text_route_title);
            description = itemView.findViewById(R.id.text_route_description);
            distanceChip = itemView.findViewById(R.id.chip_distance);
            categoryChip = itemView.findViewById(R.id.chip_category);
            viewLocationButton = itemView.findViewById(R.id.button_view_route);
            sustainabilityChip = itemView.findViewById(R.id.chip_sustainability);
        }
    }
}
