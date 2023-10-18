package com.example.pinellia.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pinellia.R;
import com.example.pinellia.databinding.ItemSymptomButtonBinding;
import java.util.List;

public class SymptomAdapter extends RecyclerView.Adapter<SymptomAdapter.SymptomViewHolder> {

    private List<String> symptomsList;

    public SymptomAdapter(List<String> symptomsList) {
        this.symptomsList = symptomsList;
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

        // Set an OnCheckedChangeListener to handle the color change
        holder.binding.toggleButtonSymptom.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                holder.binding.toggleButtonSymptom.setBackgroundResource(R.drawable.symptom_toogle_button);
                int whiteColour = ContextCompat.getColor(holder.binding.toggleButtonSymptom.getContext(), R.color.white);
                holder.binding.toggleButtonSymptom.setTextColor(whiteColour);
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
