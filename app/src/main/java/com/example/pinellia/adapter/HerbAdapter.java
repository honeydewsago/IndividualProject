package com.example.pinellia.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pinellia.HerbDetails;
import com.example.pinellia.databinding.CardviewHerbItemBinding;
import com.example.pinellia.model.Herb;

import java.util.List;

public class HerbAdapter extends RecyclerView.Adapter<HerbAdapter.HerbViewHolder> {
    private List<Herb> herbList;
    private OnItemClickListener itemClickListener;

    public HerbAdapter(List<Herb> herbList) {
        this.herbList = herbList;
    }

    @NonNull
    @Override
    public HerbViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        CardviewHerbItemBinding binding = CardviewHerbItemBinding.inflate(layoutInflater, parent, false);
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
        private final CardviewHerbItemBinding binding;

        HerbViewHolder(@NonNull CardviewHerbItemBinding binding) {
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
        }
    }
}