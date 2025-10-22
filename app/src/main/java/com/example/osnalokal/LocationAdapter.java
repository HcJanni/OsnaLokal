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

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    // Interface für Klicks, um die MainActivity zu benachrichtigen
    public interface OnLocationClickListener {
        void onLocationClick(Location location);
    }

    private List<Location> locationList;
    private final OnLocationClickListener clickListener;

    public LocationAdapter(List<Location> locationList, OnLocationClickListener clickListener) {
        this.locationList = locationList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route_card, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        Location currentLocation = locationList.get(position);

        // Binde die neuen, reichhaltigen Daten an die Views der Karte
        holder.title.setText(currentLocation.getName());
        holder.description.setText(currentLocation.getEssensart()); // z.B. Essensart als Beschreibung
        holder.distance.setText(currentLocation.getPreisspanne() + "€"); // z.B. Preisspanne als Distanz-Ersatz
        holder.categoryTag.setText(currentLocation.getArt()); // z.B. "Restaurant", "Bar"

        // Setze ein Standard-Bild, da wir noch keine Bilder pro Location haben
        holder.image.setImageResource(R.drawable.rec_tours_testimg);

        // Klick-Listener auf den Button
        holder.viewLocationButton.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onLocationClick(currentLocation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    // Methode zum Aktualisieren der Liste für die Filterung
    public void filterList(List<Location> filteredList) {
        this.locationList = filteredList;
        notifyDataSetChanged();
    }

    // ViewHolder, der die Views der Karte hält
    static class LocationViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView description;
        Chip distance;
        Chip categoryTag;
        Button viewLocationButton;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_route);
            title = itemView.findViewById(R.id.text_route_title);
            description = itemView.findViewById(R.id.text_route_description);
            distance = itemView.findViewById(R.id.chip_distance);
            categoryTag = itemView.findViewById(R.id.chip_category);
            viewLocationButton = itemView.findViewById(R.id.button_view_route);
        }
    }
}
