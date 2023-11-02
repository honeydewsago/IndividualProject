package com.example.pinellia.ui.recognition;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinellia.R;
import com.example.pinellia.adapter.HerbResultAdapter;
import com.example.pinellia.databinding.ActivityRecognitionResultsBinding;
import com.example.pinellia.model.Herb;
import com.example.pinellia.ui.BrowseHistoryActivity;
import com.example.pinellia.ui.HerbDetailsActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecognitionResultsActivity extends AppCompatActivity {

    private ActivityRecognitionResultsBinding binding;
    private TFLiteModelExecutor tfliteModelExecutor;
    private List<Map.Entry<String, Float>> finalizedLabels = new ArrayList<>();
    private List<Pair<Herb, Float>> herbResultList = new ArrayList<>();
    private List<Pair<Herb, Float>> topHerbResults = new ArrayList<>();
    private HerbResultAdapter herbRecognitionResultsAdapter;
    private int MAX_RESULT = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRecognitionResultsBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);

        // Enable action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Classification Results");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Retrieve the image path from the recognition fragment
        String imagePath = getIntent().getStringExtra("imagePath");

        //Display the image
        loadAndDisplayImage(imagePath);

        // Show the progress bar
        binding.progressBar.setVisibility(View.VISIBLE);

        // Set up RecyclerView to display herbs recognition results
        binding.recyclerViewHerbRecognition.setLayoutManager(new LinearLayoutManager(this));
        herbRecognitionResultsAdapter = new HerbResultAdapter(topHerbResults);
        binding.recyclerViewHerbRecognition.setAdapter(herbRecognitionResultsAdapter);

        herbRecognitionResultsAdapter.setOnItemClickListener(new HerbResultAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Herb herb) {
                // Launch HerbDetailsActivity activity and pass the clicked herb data
                Intent intent = new Intent(RecognitionResultsActivity.this, HerbDetailsActivity.class);
                intent.putExtra("herb", herb);
                startActivity(intent);
            }
        });

        // Execute the background tasks for classification
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {

                //Background processing
                try {
                    tfliteModelExecutor = new TFLiteModelExecutor(RecognitionResultsActivity.this);
                } catch (IOException e) {
                    Log.e("tflite", "Failed to initialize an image classifier.");
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //UI Thread processing
                        if (tfliteModelExecutor != null) {
                            classifyImage(imagePath);

                            // Hide the progress bar after classification is done
                            binding.progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }

    private void classifyImage(String path) {
        if (tfliteModelExecutor == null) {
            Toast.makeText(this, "Uninitialized tflite model or invalid context.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert the captured image to a bitmap
        Bitmap capturedBitmap = BitmapFactory.decodeFile(path);

        // Resize and preprocess the image
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(capturedBitmap, TFLiteModelExecutor.IMG_SIZE_X, TFLiteModelExecutor.IMG_SIZE_Y, true);

        // Get the prediction results labels
        finalizedLabels = tfliteModelExecutor.runInference(resizedBitmap);

        // Retrieve the predicted herbs from database
        tfliteModelExecutor.retrieveAllHerbs(new TFLiteModelExecutor.AllHerbsCallback() {
            @Override
            public void onAllHerbsRetrieved(List<Herb> herbList) {
                List<Pair<Herb, Float>> accurateResults = new ArrayList<>();

                for (Map.Entry<String, Float> entry : finalizedLabels) {
                    for (Herb herb : herbList) {
                        String herbName = entry.getKey();
                        if (herb.getName().equals(herbName)) {
                            // Herb name matches a name in finalizedLabels, add it to herbResultList
                            herbResultList.add(new Pair<>(herb, entry.getValue()));
                        }
                    }
                }

                for (Pair<Herb, Float> herbPair : herbResultList) {
                    // Convert the Float probability to Double
                    Double probability = (double) herbPair.second * 100.0; // Convert to percentage

                    if (probability > 50.0) {
                        // Add herbs with probabilities greater than 50
                        accurateResults.add(new Pair<>(herbPair.first, herbPair.second));
                        Log.d("tflite", "Accurate Herb Name: " + herbPair.first.getName() + ", Probability: " + herbPair.second + "%");
                    }
                }

                // Display the top results, if available
                for (int i = 0; i < Math.min(MAX_RESULT, accurateResults.size()); i++) {
                    topHerbResults.add(accurateResults.get(i));
                }

                herbRecognitionResultsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onAllHerbsError(Exception e) {
                // Handle the error
                Toast.makeText(RecognitionResultsActivity.this, "Error retrieving herbs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        resizedBitmap.recycle();
    }

    private void loadAndDisplayImage(String imagePath) {
        if (imagePath == null) {
            // Handle the case where the imagePath is null or invalid
            return;
        }

        // Load and display the image using Glide
        RequestOptions requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache the image
                .placeholder(R.drawable.bg_light_green_gradient) // Placeholder while loading
                .error(R.drawable.bg_light_green_gradient); // Error placeholder

        // Load the herb image
        Glide.with(this)
                .load("file://" + imagePath) // Load image from the file path
                .apply(requestOptions)
                .into(binding.imageViewCaptured);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle the back button click in the action bar
        onBackPressed();
        return true;
    }
}