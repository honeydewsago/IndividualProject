package com.example.pinellia.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinellia.R;
import com.example.pinellia.databinding.ItemHerbCardviewBinding;
import com.example.pinellia.model.Herb;

import java.util.ArrayList;
import java.util.List;

public class HerbAdapter extends RecyclerView.Adapter<HerbAdapter.HerbViewHolder> {
    private List<Herb> herbList;
    private OnItemClickListener itemClickListener;

    public HerbAdapter(List<Herb> herbList) {
        this.herbList = herbList;
    }

    // Custom method to update the data in the adapter
    public void updateData(List<Herb> newHerbList) {
        herbList.clear();
        herbList.addAll(newHerbList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HerbViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemHerbCardviewBinding binding = ItemHerbCardviewBinding.inflate(layoutInflater, parent, false);
        return new HerbViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HerbViewHolder holder, int position) {
        Herb herb = herbList.get(position);
        holder.bind(herb);
    }

    @Override
    public int getItemCount() {
        return herbList.size();
    }

    // Click listener interface
    public interface OnItemClickListener {
        void onItemClick(Herb herb);
    }

    // Set the click listener
    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public class HerbViewHolder extends RecyclerView.ViewHolder {
        private final ItemHerbCardviewBinding binding;

        HerbViewHolder(@NonNull ItemHerbCardviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION) {
                        Herb clickedHerb = herbList.get(position);

                        // Notify the click listener that an item has been clicked
                        if (itemClickListener != null) {
                            itemClickListener.onItemClick(clickedHerb);
                        }
                    }
                }
            });
        }

        void bind(Herb herb) {
            binding.textViewName.setText(herb.getName());

            // Check if the symptoms list is empty and update visibility
            if (herb.getSymptomsList() != null) {
                if (!herb.getSymptomsList().isEmpty()) {
                    binding.textViewSymptoms.setVisibility(View.VISIBLE);
                    binding.textViewSymptoms.setText("Symptoms: " + TextUtils.join(", ", herb.getSymptomsList()));
                }
            } else {
                binding.textViewSymptoms.setVisibility(View.GONE);
            }

            // Load the image from Firebase Storage using Glide
            String imageLink = herb.getImageLink();

            RequestOptions requestOptions = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache image
                    .placeholder(R.drawable.white_box_bg) // Placeholder while loading
                    .error(R.drawable.white_box_bg); // Error placeholder

            Glide.with(binding.getRoot().getContext())
                    .load(imageLink)
                    .apply(requestOptions)
                    .into(binding.imageViewHerb);
        }
    }
}