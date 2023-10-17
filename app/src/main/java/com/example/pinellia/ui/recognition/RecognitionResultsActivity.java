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
//                            Bitmap bitmap = processImageToBGR(imagePath);
//                            binding.imageViewProcess.setImageBitmap(bitmap);


                            classifyImage(imagePath);

//                            Bitmap bitmap = processImageToBGRWithImageNet(imagePath);
//                            binding.imageViewProcess.setImageBitmap(bitmap);
//                            classifyImageResNet(bitmap);

                            // Hide the progress bar after classification is done
                            binding.progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }

    static float[][][] imagenet_preprocess_input_caffe2(Bitmap bitmap) {
        final float[] imagenet_means_caffe = new float[]{103.939f, 116.779f, 123.68f};

        float[][][] result = new float[bitmap.getHeight()][bitmap.getWidth()][3]; // Assuming RGB

        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {
                final int px = bitmap.getPixel(x, y);

                // Convert RGB to BGR
                result[y][x][0] = (Color.red(px) - imagenet_means_caffe[2]) / 255.0f;  // B
                result[y][x][1] = (Color.green(px) - imagenet_means_caffe[1]) / 255.0f; // G
                result[y][x][2] = (Color.blue(px) - imagenet_means_caffe[0]) / 255.0f;  // R
            }
        }

        return result;
    }

    static float[][][] imagenet_preprocess_input_caffe( Bitmap bitmap ) {
        final float[] imagenet_means_caffe = new float[]{103.939f, 116.779f, 123.68f};

        float[][][] result = new float[bitmap.getHeight()][bitmap.getWidth()][3];   // assuming rgb
        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {

                final int px = bitmap.getPixel(x, y);

                // rgb-->bgr, then subtract means.  no scaling
                result[y][x][0] = (Color.blue(px) - imagenet_means_caffe[0] );
                result[y][x][1] = (Color.green(px) - imagenet_means_caffe[1] );
                result[y][x][2] = (Color.red(px) - imagenet_means_caffe[2] );
            }
        }

        return result;
    }

    private Bitmap processImageToBGRWithImageNet(String path) {
        Log.d("tflite", "Processing image to BGR");

        // Convert the captured image to a bitmap
        Bitmap capturedBitmap = BitmapFactory.decodeFile(path);

        // Check if decoding the image was successful
        if (capturedBitmap == null) {
            Log.e("tflite", "Failed to decode the image.");
            return null;
        }

        // Preprocess the captured image using imagenet_preprocess_input_caffe
        float[][][] preprocessedImage = imagenet_preprocess_input_caffe2(capturedBitmap);

        // Create a new Bitmap with the same dimensions as the captured image
        Bitmap resizedBitmap = Bitmap.createBitmap(capturedBitmap.getWidth(), capturedBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        for (int y = 0; y < preprocessedImage.length; y++) {
            for (int x = 0; x < preprocessedImage[y].length; x++) {
                int b = (int) preprocessedImage[y][x][0];
                int g = (int) preprocessedImage[y][x][1];
                int r = (int) preprocessedImage[y][x][2];

                // Ensure the values are within the 0-255 range
                b = Math.max(0, Math.min(255, b));
                g = Math.max(0, Math.min(255, g));
                r = Math.max(0, Math.min(255, r));

                // Create a color pixel and set it in the Bitmap
                int colorPixel = Color.rgb(r, g, b);
                resizedBitmap.setPixel(x, y, colorPixel);
            }
        }

        return resizedBitmap;
    }

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
    }

    private void classifyImageResNet(Bitmap bitmap) {
        if (tfliteModelExecutor == null) {
            Toast.makeText(this, "Uninitialized tflite model or invalid context.", Toast.LENGTH_SHORT).show();
            return;
        }

        String textToShow = tfliteModelExecutor.runInference(bitmap);
//        bitmap.recycle();

        binding.textViewResults.setText(textToShow);
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