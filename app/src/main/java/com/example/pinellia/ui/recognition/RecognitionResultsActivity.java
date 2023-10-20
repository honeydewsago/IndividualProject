package com.example.pinellia.ui.recognition;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
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
import com.example.pinellia.databinding.ActivityRecognitionResultsBinding;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecognitionResultsActivity extends AppCompatActivity {

    private ActivityRecognitionResultsBinding binding;
    private TFLiteModelExecutor tfliteModelExecutor;

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

        String textToShow = tfliteModelExecutor.runInference(resizedBitmap);
        resizedBitmap.recycle();

        binding.textViewResults.setText(textToShow);
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

        Glide.with(this)
                .load("file://" + imagePath) // Load image from the file path
                .apply(requestOptions)
                .into(binding.imageViewCaptured); // Set the loaded image to your ImageView
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle the back button click in the action bar
        onBackPressed();
        return true;
    }
}