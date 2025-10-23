package com.example.osnalokal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    public interface OnNewsClickListener {
        void onNewsClick(NewsItem newsItem);
    }
    private final List<NewsItem> newsList;
    private final OnNewsClickListener clickListener;

    // Konstruktor: Der Adapter erhält die Liste der anzuzeigenden News
    public NewsAdapter(List<NewsItem> newsList, OnNewsClickListener clickListener) {
        this.newsList = newsList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news_row, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem currentItem = newsList.get(position);
        // ... (Daten binden bleibt gleich)
        holder.title.setText(currentItem.getTitle());
        holder.distance.setText(currentItem.getDescription());
        Glide.with(holder.itemView.getContext())
                .load("file:///android_asset/Pictures/default.png")
                .centerCrop()
                .into(holder.image);
        // Klick-Listener auf die ganze Zeile setzen
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onNewsClick(currentItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    // Hält die Referenzen auf die UI-Elemente einer einzelnen Zeile
    static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView distance;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_news);
            title = itemView.findViewById(R.id.text_news_title);
            distance = itemView.findViewById(R.id.text_news_distance);
        }
    }
}
