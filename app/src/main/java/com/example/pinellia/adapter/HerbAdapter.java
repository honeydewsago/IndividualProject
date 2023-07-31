package com.example.pinellia.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pinellia.databinding.CardviewHerbItemBinding;
import com.example.pinellia.model.Herb;

import java.util.List;

public class HerbAdapter extends RecyclerView.Adapter<HerbAdapter.HerbViewHolder> {
    private List<Herb> herbList;

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

    static class HerbViewHolder extends RecyclerView.ViewHolder {
        private final CardviewHerbItemBinding binding;

        HerbViewHolder(@NonNull CardviewHerbItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Herb herb) {
            binding.textViewName.setText(herb.getName());
            binding.textViewDescription.setText(herb.getDescription());
        }
    }
}
