package com.example.yourselfie;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SmileyAdapter extends RecyclerView.Adapter<SmileyAdapter.FilterViewHolder> {

    private List<Integer> filterImages;
    private LayoutInflater inflater;
    private OnItemClickListener itemClickListener;



    public SmileyAdapter(Context context, List<Integer> filterImages) {
        this.filterImages = filterImages;
        inflater = LayoutInflater.from(context);
    }
    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public FilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new FilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterViewHolder holder, int position) {
        int imagePosition = position % filterImages.size();
        int imageResId = filterImages.get(imagePosition);
        holder.filterImageView.setImageResource(filterImages.get(position));
    //Sets click listener for items
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filterImages.size();
    }

    class FilterViewHolder extends RecyclerView.ViewHolder {
        ImageView filterImageView;

        FilterViewHolder(@NonNull View itemView) {
            super(itemView);
            filterImageView = itemView.findViewById(R.id.filterImageView);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}

