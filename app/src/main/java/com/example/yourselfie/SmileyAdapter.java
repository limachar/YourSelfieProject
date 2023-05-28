package com.example.yourselfie;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SmileyAdapter extends RecyclerView.Adapter<SmileyAdapter.FilterViewHolder> {

    private List<Integer> filterImages;
    private LayoutInflater inflater;

    public SmileyAdapter(Context context, List<Integer> filterImages) {
        this.filterImages = filterImages;
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public FilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new FilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterViewHolder holder, int position) {
        holder.filterImageView.setImageResource(filterImages.get(position));
    }

    @Override
    public int getItemCount() {
        return filterImages.size();
    }

    class FilterViewHolder extends RecyclerView.ViewHolder {
        ImageView filterImageView;

        FilterViewHolder(@NonNull View itemView) {
            super(itemView);
            filterImageView = itemView.findViewById(R.id.image_view_for_list);
        }
    }
}

