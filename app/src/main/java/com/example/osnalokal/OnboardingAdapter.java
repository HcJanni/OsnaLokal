package com.example.osnalokal;

import android.view.LayoutInflater;import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    // Daten für die Onboarding-Seiten
    private final String[] titles = {"Entdecke Osnabrück!"};
    private final String[] descriptions = {
            "Schaue dir neue Orte an, entdecke neue Lokale und habe Spaß beim entdecken."
    };
    private final int[] images = {
            R.drawable.startupscreenimg // Ersetze dies mit deinem Logo/Bild
    };


    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.onboarding_page, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        holder.title.setText(titles[position]);
        holder.description.setText(descriptions[position]);
        holder.image.setImageResource(images[position]);
    }

    @Override
    public int getItemCount() {
        return titles.length; // Anzahl der Seiten
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView description;

        OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageViewOnboarding);
            title = itemView.findViewById(R.id.textViewOnboardingTitle);
            description = itemView.findViewById(R.id.textViewOnboardingDescription);
        }
    }
}
