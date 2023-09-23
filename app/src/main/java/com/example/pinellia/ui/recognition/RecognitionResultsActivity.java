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
                            Bitmap bitmap = processImageToBGR(imagePath);
                            binding.imageViewProcess.setImageBitmap(bitmap);

                            classifyImage(bitmap);

                            // Hide the progress bar after classification is done
                            binding.progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }

//    private void displayProcessedImage(String path) {
//        Log.d("tflite", "Displaying processed image");
//
//        // Convert the captured image to a bitmap
//        Bitmap capturedBitmap = BitmapFactory.decodeFile(path);
//
//        ByteBuffer processedBuffer = tfliteModelExecutor.preprocessImageForResNet50(capturedBitmap);
//
//        // Convert the ByteBuffer to float values
//        float[] floatValues = new float[TFLiteModelExecutor.IMG_SIZE_X * TFLiteModelExecutor.IMG_SIZE_Y * TFLiteModelExecutor.PIXEL_SIZE];
//        processedBuffer.rewind();
//        processedBuffer.asFloatBuffer().get(floatValues);
//
//        // Convert float values to ARGB pixel values
//        int[] intValues = new int[TFLiteModelExecutor.IMG_SIZE_X * TFLiteModelExecutor.IMG_SIZE_Y];
//        for (int i = 0; i < intValues.length; ++i) {
//            intValues[i] = Color.argb(255,
//                    (int) (floatValues[i] * TFLiteModelExecutor.IMG_STD + TFLiteModelExecutor.IMG_MEAN),
//                    (int) (floatValues[i + intValues.length] * TFLiteModelExecutor.IMG_STD + TFLiteModelExecutor.IMG_MEAN),
//                    (int) (floatValues[i + 2 * intValues.length] * TFLiteModelExecutor.IMG_STD + TFLiteModelExecutor.IMG_MEAN));
//        }
//
//        // Create a Bitmap from the ARGB pixel values
//        Bitmap bitmap = Bitmap.createBitmap(intValues, TFLiteModelExecutor.IMG_SIZE_X, TFLiteModelExecutor.IMG_SIZE_Y, Bitmap.Config.ARGB_8888);
//
//        // Display the processed image in the ImageView
//        binding.imageViewProcess.setImageBitmap(bitmap);
//
//    }

    // Function to convert a ByteBuffer to a Bitmap
//    private Bitmap convertByteBufferToBitmap(ByteBuffer buffer) {
//        Log.d("tflite", "Converting byte buffer to bitmap");
//        int[] intValues = new int[TFLiteModelExecutor.IMG_SIZE_X * TFLiteModelExecutor.IMG_SIZE_Y];
//        float[] floatValues = new float[intValues.length];
//
//        // Convert the ByteBuffer to float values
//        buffer.rewind();
//        buffer.asFloatBuffer().get(floatValues);
//
//        // Convert float values to ARGB pixel values
//        for (int i = 0; i < intValues.length; ++i) {
//            intValues[i] = Color.argb(255,
//                    (int) (floatValues[i] * TFLiteModelExecutor.IMG_STD + TFLiteModelExecutor.IMG_MEAN),
//                    (int) (floatValues[i + intValues.length] * TFLiteModelExecutor.IMG_STD + TFLiteModelExecutor.IMG_MEAN),
//                    (int) (floatValues[i + 2 * intValues.length] * TFLiteModelExecutor.IMG_STD + TFLiteModelExecutor.IMG_MEAN));
//        }
//
//        // Create a Bitmap from the ARGB pixel values
//        Bitmap bitmap = Bitmap.createBitmap(intValues, TFLiteModelExecutor.IMG_SIZE_X, TFLiteModelExecutor.IMG_SIZE_Y, Bitmap.Config.ARGB_8888);
//
//        return bitmap;
//    }

    private Bitmap processImageToBGR(String path) {
        Log.d("tflite", "Displaying processed image");

        // Convert the captured image to a bitmap
        Bitmap capturedBitmap = BitmapFactory.decodeFile(path);

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(capturedBitmap, TFLiteModelExecutor.IMG_SIZE_X, TFLiteModelExecutor.IMG_SIZE_Y, true);

        // Convert ARGB to BGR and normalize pixel values
        int width = resizedBitmap.getWidth();
        int height = resizedBitmap.getHeight();
        int[] pixels = new int[width * height];
        resizedBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        // Convert ARGB to BGR, subtract mean values, and normalize pixel values
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int blue = (pixel >> 16) & 0xFF;
            int green = (pixel >> 8) & 0xFF;
            int red = pixel & 0xFF;

            // Convert to BGR format, subtract mean values, and normalize
            float blueNorm = (blue - 103.939f) / 255.0f;
            float greenNorm = (green - 116.779f) / 255.0f;
            float redNorm = (red - 123.68f) / 255.0f;

            // Clamp values to the range [0, 1]
            blueNorm = Math.max(0.0f, Math.min(1.0f, blueNorm));
            greenNorm = Math.max(0.0f, Math.min(1.0f, greenNorm));
            redNorm = Math.max(0.0f, Math.min(1.0f, redNorm));

            // Convert back to ARGB format
            int blueFinal = (int) (blueNorm * 255.0f);
            int greenFinal = (int) (greenNorm * 255.0f);
            int redFinal = (int) (redNorm * 255.0f);

            pixels[i] = 0xFF000000 | (redFinal << 16) | (greenFinal << 8) | blueFinal;
        }

        resizedBitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        return resizedBitmap;
        // Display the processed image in the ImageView
//        binding.imageViewProcess.setImageBitmap(resizedBitmap);
    }

    private void classifyImage(Bitmap bitmap) {
        if (tfliteModelExecutor == null) {
            Toast.makeText(this, "Uninitialized tflite model or invalid context.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert the captured image to a bitmap
//        Bitmap capturedBitmap = BitmapFactory.decodeFile(path);

        // Resize and preprocess the image
//        Bitmap resizedBitmap = Bitmap.createScaledBitmap(capturedBitmap, TFLiteModelExecutor.IMG_SIZE_X, TFLiteModelExecutor.IMG_SIZE_Y, true);

        String textToShow = tfliteModelExecutor.runInference(bitmap);
        bitmap.recycle();

        binding.textViewResults.setText(textToShow);
    }

//    private void classifyImage(String path) {
//        if (tfliteModelExecutor == null) {
//            Toast.makeText(this, "Uninitialized tflite model or invalid context.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Convert the captured image to a bitmap
//        Bitmap capturedBitmap = BitmapFactory.decodeFile(path);
//
//        // Resize and preprocess the image
//        Bitmap resizedBitmap = Bitmap.createScaledBitmap(capturedBitmap, TFLiteModelExecutor.IMG_SIZE_X, TFLiteModelExecutor.IMG_SIZE_Y, true);
//
//        String textToShow = tfliteModelExecutor.runInference(resizedBitmap);
//        resizedBitmap.recycle();
//
//        binding.textViewResults.setText(textToShow);
//    }

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