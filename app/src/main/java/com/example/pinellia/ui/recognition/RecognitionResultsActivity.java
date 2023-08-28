package com.example.pinellia.ui.recognition;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.pinellia.databinding.ActivityRecognitionResultsBinding;

import java.io.IOException;
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
        Toast.makeText(this, textToShow+"", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle the back button click in the action bar
        onBackPressed();
        return true;
    }
}