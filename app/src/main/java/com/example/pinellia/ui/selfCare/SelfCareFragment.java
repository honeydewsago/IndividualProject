package com.example.pinellia.ui.selfCare;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pinellia.databinding.FragmentSelfCareBinding;

public class SelfCareFragment extends Fragment {

    private FragmentSelfCareBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SelfCareViewModel selfcareViewModel =
                new ViewModelProvider(this).get(SelfCareViewModel.class);

        binding = FragmentSelfCareBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.relativeLayoutSymptom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to launch the SymptomActivity
                Intent intent = new Intent(v.getContext(), SymptomActivity.class);

                // Start the SymptomActivity
                startActivity(intent);
            }
        });

        binding.relativeLayoutUsage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to launch the UsageActivity
                Intent intent = new Intent(v.getContext(), UsageActivity.class);

                // Start the UsageActivity
                startActivity(intent);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}