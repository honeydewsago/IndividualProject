package com.example.pinellia.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pinellia.databinding.ItemMeridianFlavourBinding;

import java.util.List;

public class MeridianTropismAdapter extends RecyclerView.Adapter<MeridianTropismAdapter.ViewHolder> {

    private List<String> meridianTropismList;

    public MeridianTropismAdapter(List<String> meridianTropismList) {
        this.meridianTropismList = meridianTropismList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemMeridianFlavourBinding binding = ItemMeridianFlavourBinding.inflate(layoutInflater, parent, false);
        return new MeridianTropismAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String meridianTropism = meridianTropismList.get(position);
        holder.textViewMeridianTropism.setText(meridianTropism);
    }

    @Override
    public int getItemCount() {
        return meridianTropismList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemMeridianFlavourBinding binding;
        TextView textViewMeridianTropism;

        public ViewHolder(ItemMeridianFlavourBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            textViewMeridianTropism = binding.tvMeridianFlavourItem;
        }
    }
}
