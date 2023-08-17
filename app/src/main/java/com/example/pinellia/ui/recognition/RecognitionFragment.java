package com.example.pinellia.ui.recognition;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.example.pinellia.TFLiteModelExecutor;
import com.example.pinellia.databinding.FragmentRecognitionBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecognitionFragment extends Fragment {

    private static final String TAG = "ProcessCaptureImage";
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private FragmentRecognitionBinding binding;
    private RecognitionViewModel recognitionViewModel;
    private ExecutorService cameraExecutor;
    private TFLiteModelExecutor mTFLiteModelExecutor;
    private int inputHeight = 224;
    private int inputWidth  = 224;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recognitionViewModel = new ViewModelProvider(this).get(RecognitionViewModel.class);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentRecognitionBinding.inflate(inflater, container, false);
//        View root =

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            recognitionViewModel.setCameraPermissionGranted(true);
            startCamera();
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }

        binding.captureButton.setOnClickListener(v -> {
            takePhoto(); // Capture the image
            // Call the processCapturedImage function with the captured image path
            processCapturedImage(recognitionViewModel.getCapturedImagePath());
        });

        // Initialize TFLiteModelExecutor using the model file from assets
        String modelFileName = "model.tflite"; // Replace with your actual model file name
        String modelAssetPath = getAssetFilePath(modelFileName);
        mTFLiteModelExecutor = new TFLiteModelExecutor(modelAssetPath);
    }

    private String getAssetFilePath(String assetFileName) {
        AssetManager assetManager = requireContext().getAssets();
        try {
            return assetManager.openFd(assetFileName).getFileDescriptor().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void processCapturedImage(String imagePath) {
        try {
            Bitmap capturedBitmap = BitmapFactory.decodeFile(imagePath);
            if (capturedBitmap != null) {
                // Resize the bitmap to the required input size of tflite model
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                        capturedBitmap, inputWidth, inputHeight, true);

                // Convert the resized bitmap to a float array
                float[] inputArray = convertBitmapToFloatArray(resizedBitmap);

                // Make predictions using the TFLite model
                float[] predictions = mTFLiteModelExecutor.runInference(inputArray);

                // Process the predictions (e.g., find the highest confidence class)
                int predictedClass = findPredictedClass(predictions);

                // Perform actions based on the predicted class (e.g., update UI)
                updateUIWithPrediction(predictedClass);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing captured image: " + e.getMessage());
        }
    }

    private float[] convertBitmapToFloatArray(Bitmap bitmap) {
        // Convert the bitmap to a float array and normalize pixel values
        // Normalize pixel values to [0, 1] and reshape into a 1D array
        float[] floatArray = new float[inputWidth * inputHeight * 3];
        int index = 0;

        for (int y = 0; y < inputHeight; y++) {
            for (int x = 0; x < inputWidth; x++) {
                int pixel = bitmap.getPixel(x, y);

                // Normalize pixel values to the range [0, 1]
                float red = ((pixel >> 16) & 0xFF) / 255.0f;
                float green = ((pixel >> 8) & 0xFF) / 255.0f;
                float blue = (pixel & 0xFF) / 255.0f;

                // Add normalized values to the float array
                floatArray[index++] = red;
                floatArray[index++] = green;
                floatArray[index++] = blue;
            }
        }

        return floatArray;
    }

    private int findPredictedClass(float[] predictions) {
        // Find the index of the class with the highest prediction score
        int predictedClass = 0;
        float maxScore = predictions[0];
        for (int i = 1; i < predictions.length; i++) {
            if (predictions[i] > maxScore) {
                predictedClass = i;
                maxScore = predictions[i];
            }
        }
        return predictedClass;
    }

    private void updateUIWithPrediction(int predictedClass) {
        // Update the UI based on the predicted class
        // For example, update a TextView with the predicted herb's name
        // ...
        Toast.makeText(getContext(), "class: "+predictedClass, Toast.LENGTH_SHORT).show();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                    CameraSelector cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build();

                    ImageCapture imageCapture = new ImageCapture.Builder().build();

                    Camera camera = cameraProvider.bindToLifecycle(
                            (LifecycleOwner) requireContext(),
                            cameraSelector,
                            imageCapture);
                } catch (Exception e) {
                    // Handle exception
                }
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {
        File photoFile = createFile();
        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        ImageCapture imageCapture = new ImageCapture.Builder().build();
        imageCapture.takePicture(outputFileOptions, cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        String path = photoFile.getAbsolutePath();
                        recognitionViewModel.setCapturedImagePath(path);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        // Handle error
                    }
                });
    }

    private File createFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis());
        File storageDir = requireActivity().getExternalFilesDir(null);
        return new File(storageDir, "IMG_" + timeStamp + ".jpg");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}