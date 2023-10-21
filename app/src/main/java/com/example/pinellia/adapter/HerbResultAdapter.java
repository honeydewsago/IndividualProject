package com.example.pinellia.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinellia.R;
import com.example.pinellia.databinding.ItemHerbCardviewBinding;
import com.example.pinellia.databinding.ItemHerbRecognitionCardviewBinding;
import com.example.pinellia.model.Herb;

import java.util.List;

public class HerbResultAdapter extends RecyclerView.Adapter<HerbResultAdapter.HerbResultViewHolder> {
    private List<Pair<Herb, Float>> herbResultList;
    private OnItemClickListener itemClickListener;

    public HerbResultAdapter(List<Pair<Herb, Float>> herbResultList) {
        this.herbResultList = herbResultList;
    }

    @NonNull
    @Override
    public HerbResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemHerbRecognitionCardviewBinding binding = ItemHerbRecognitionCardviewBinding.inflate(layoutInflater, parent, false);
        return new HerbResultAdapter.HerbResultViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HerbResultViewHolder holder, int position) {
        Pair<Herb, Float> herbResult = herbResultList.get(position);

        // Bind data to the ViewHolder
        holder.bind(herbResult);
    }

    @Override
    public int getItemCount() {
        return herbResultList.size();
    }

    // Click listener interface
    public interface OnItemClickListener {
        void onItemClick(Herb herb);
    }

    // Set the click listener
    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public class HerbResultViewHolder extends RecyclerView.ViewHolder {
        private final ItemHerbRecognitionCardviewBinding binding;

        public HerbResultViewHolder(ItemHerbRecognitionCardviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION) {
                        Pair<Herb, Float> selectedHerb = herbResultList.get(position);

                        // Notify the click listener that an item has been clicked
                        if (itemClickListener != null) {
                            itemClickListener.onItemClick(selectedHerb.first);
                        }
                    }
                }
            });
        }

        public void bind(Pair<Herb, Float> herbResult) {
            binding.textViewName.setText(herbResult.first.getName());

            // Format the probability to a percentage
            double probability = herbResult.second * 100;
            binding.textViewAccuracy.setText(String.format("%.2f%%", probability));

            // Load the image from Firebase Storage using Glide
            String imageLink = herbResult.first.getImageLink();

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
