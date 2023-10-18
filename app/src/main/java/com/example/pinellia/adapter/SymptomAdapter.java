package com.example.pinellia.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pinellia.R;
import com.example.pinellia.databinding.ItemSymptomButtonBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SymptomAdapter extends RecyclerView.Adapter<SymptomAdapter.SymptomViewHolder> {

    private List<String> symptomsList;
    private List<Boolean> selectedItems;

    public SymptomAdapter(List<String> symptomsList) {
        this.symptomsList = symptomsList;
        selectedItems = new ArrayList<>(Collections.nCopies(symptomsList.size(), false));
    }

    // Add a method to get the selected items
    public List<String> getSelectedItems() {
        List<String> selectedSymptoms = new ArrayList<>();
        for (int i = 0; i < selectedItems.size(); i++) {
            if (selectedItems.get(i)) {
                selectedSymptoms.add(symptomsList.get(i));
            }
        }
        return selectedSymptoms;
    }

    @Override
    public SymptomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemSymptomButtonBinding binding = ItemSymptomButtonBinding.inflate(layoutInflater, parent, false);
        return new SymptomAdapter.SymptomViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SymptomViewHolder holder, int position) {
        String symptom = symptomsList.get(position);
        holder.binding.toggleButtonSymptom.setText(symptom);
        holder.binding.toggleButtonSymptom.setTextOn(symptom);
        holder.binding.toggleButtonSymptom.setTextOff(symptom);

        // Set the selected state
        holder.binding.toggleButtonSymptom.setChecked(selectedItems.get(position));

        // Set an OnCheckedChangeListener to handle the color change
        holder.binding.toggleButtonSymptom.setOnCheckedChangeListener((buttonView, isChecked) -> {
            selectedItems.set(position, isChecked);
            if (isChecked) {
                holder.binding.toggleButtonSymptom.setBackgroundResource(R.drawable.symptom_toogle_button);
                int whiteColour = ContextCompat.getColor(holder.binding.toggleButtonSymptom.getContext(), R.color.white);
                holder.binding.toggleButtonSymptom.setTextColor(whiteColour);

                // Apply a scale animation when checked
                Animation animation = AnimationUtils.loadAnimation(holder.binding.toggleButtonSymptom.getContext(), R.anim.scale_up);
                holder.binding.toggleButtonSymptom.startAnimation(animation);

            } else {
                holder.binding.toggleButtonSymptom.setBackgroundResource(R.drawable.symptom_toogle_button);
                int blackColour = ContextCompat.getColor(holder.binding.toggleButtonSymptom.getContext(), R.color.dark_black);
                holder.binding.toggleButtonSymptom.setTextColor(blackColour);
            }
        });
    }

    @Override
    public int getItemCount() {
        return symptomsList.size();
    }

    public class SymptomViewHolder extends RecyclerView.ViewHolder {
        private final ItemSymptomButtonBinding binding;

        public SymptomViewHolder(ItemSymptomButtonBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
