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

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.example.pinellia.MainActivity;
import com.example.pinellia.TFLiteModelExecutor;
import com.example.pinellia.databinding.FragmentRecognitionBinding;
import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecognitionFragment extends Fragment {

    private FragmentRecognitionBinding binding;
    private TFLiteModelExecutor tfliteExecutor;

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            if (result) {
                startCamera();
            }
        }
    });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize the TFLiteModelExecutor with the model file path
        String modelFilePath = "trained_model.tflite";
        tfliteExecutor = new TFLiteModelExecutor(requireContext(), modelFilePath);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentRecognitionBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.CAMERA);
        } else {
            startCamera();
        }
    }

    public void startCamera() {
        int aspectRatio = aspectRatio(binding.cameraPreviewView.getWidth(), binding.cameraPreviewView.getHeight());
        ListenableFuture<ProcessCameraProvider> listenableFuture = ProcessCameraProvider.getInstance(requireContext());

        listenableFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = (ProcessCameraProvider) listenableFuture.get();

                Preview preview = new Preview.Builder().setTargetAspectRatio(aspectRatio).build();

                ImageCapture imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetRotation(requireActivity().getWindowManager().getDefaultDisplay().getRotation()).build();

                CameraSelector cameraSelector = new CameraSelector.Builder().build();

                cameraProvider.unbindAll();

                Camera camera = cameraProvider.bindToLifecycle(requireActivity(), cameraSelector, preview, imageCapture);

                binding.captureButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        } else {
                            takePicture(imageCapture);
                        }
                    }
                });

                preview.setSurfaceProvider(binding.cameraPreviewView.getSurfaceProvider());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    public void takePicture(ImageCapture imageCapture) {
        final File file = new File(requireContext().getExternalFilesDir(null), System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
        imageCapture.takePicture(outputFileOptions, Executors.newCachedThreadPool(), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {

                if (tfliteExecutor == null) {
                    Log.e("RecognitionFragment", "TFLite interpreter is null. Inference cannot be performed.");
                    return;
                }

                // Convert the captured image to a bitmap
                Bitmap capturedBitmap = BitmapFactory.decodeFile(file.getPath());

                // Resize and preprocess the image as required by your model
                Bitmap resizedBitmap = preprocessImage(capturedBitmap);

                // Convert the preprocessed bitmap to a float array
                float[] inputArray = convertBitmapToFloatArray(resizedBitmap);

                // Allocate buffers for input and output tensors
                ByteBuffer inputBuffer = ByteBuffer.allocateDirect(inputArray.length * 4); // 4 bytes per float
                inputBuffer.order(ByteOrder.nativeOrder());
                inputBuffer.rewind();
                inputBuffer.asFloatBuffer().put(inputArray);

                // Perform inference using the TFLite model
                float[] prediction = tfliteExecutor.runInference(inputArray);

                // Process the prediction results
                processPrediction(prediction);

                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(requireContext(), "Image saved at: " + file.getPath(), Toast.LENGTH_SHORT).show();
                    }
                });
                startCamera();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(requireContext(), "Failed to save: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                startCamera();
            }
        });
    }

    private void processPrediction(float[] prediction) {
        // Determine the index with the highest probability
        int maxIndex = 0;
        for (int i = 1; i < prediction.length; i++) {
            if (prediction[i] > prediction[maxIndex]) {
                maxIndex = i;
            }
        }

        // Perform actions based on the prediction
        String predictedClass = getClassLabel(maxIndex); // Replace with your method to get class label
        Toast.makeText(requireContext(), "Predict: " + predictedClass, Toast.LENGTH_SHORT).show();;
    }

    private String getClassLabel(int classIndex) {
        // Map class index to corresponding class label
        String[] classLabels = {"heshouwu", "gouqi", "chongcao", "huangbai", "fuling", "dangshen", "baihe", "aiye", "huangqi", "gancao", "jinyinhua", "renshen", "shanyao", "tiannanxing", "luohanguo"};
        return classLabels[classIndex];
    }

    private Bitmap preprocessImage(Bitmap originalBitmap) {
        // Resize the image to the input size required by tflite model
        int inputWidth = 224;
        int inputHeight = 224;

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, inputWidth, inputHeight, true);

        // Perform other preprocessing steps if needed, such as normalization
        // ...

        return resizedBitmap;
    }

    private float[] convertBitmapToFloatArray(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Define the channel order (assuming RGB)
        int channelStride = width * height;

        int[] intValues = new int[channelStride];
        bitmap.getPixels(intValues, 0, width, 0, 0, width, height);

        float[] floatValues = new float[width * height * 3]; // 3 channels (RGB)
        for (int i = 0; i < intValues.length; i++) {
            final int val = intValues[i];
            floatValues[i * 3] = ((val >> 16) & 0xFF) / 255.0f;     // Red channel
            floatValues[i * 3 + 1] = ((val >> 8) & 0xFF) / 255.0f;  // Green channel
            floatValues[i * 3 + 2] = (val & 0xFF) / 255.0f;         // Blue channel
        }

        return floatValues;
    }

    private int aspectRatio(int width, int height) {
        double previewRatio = (double) Math.max(width, height) / Math.min(width, height);
        if (Math.abs(previewRatio - 4.0 / 3.0) <= Math.abs(previewRatio - 16.0 / 9.0)) {
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

//        cameraExecutor = Executors.newSingleThreadExecutor();
//
//        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
//        == PackageManager.PERMISSION_GRANTED) {
//        recognitionViewModel.setCameraPermissionGranted(true);
//        startCamera();
//        } else {
//        ActivityCompat.requestPermissions(requireActivity(),
//        new String[]{Manifest.permission.CAMERA},
//        REQUEST_CAMERA_PERMISSION);
//        }
//
//        binding.captureButton.setOnClickListener(v -> {
//        takePhoto(); // Capture the image
//        // Call the processCapturedImage function with the captured image path
//        processCapturedImage(recognitionViewModel.getCapturedImagePath());
//        });
//
//        // Initialize TFLiteModelExecutor using the model file from assets
//        String modelFileName = "model.tflite"; // Replace with your actual model file name
//        String modelAssetPath = getAssetFilePath(modelFileName);
//        mTFLiteModelExecutor = new TFLiteModelExecutor(modelAssetPath);
//
//    private String getAssetFilePath(String assetFileName) {
//        AssetManager assetManager = requireContext().getAssets();
//        try {
//            return assetManager.openFd(assetFileName).getFileDescriptor().toString();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private void processCapturedImage(String imagePath) {
//        try {
//            Bitmap capturedBitmap = BitmapFactory.decodeFile(imagePath);
//            if (capturedBitmap != null) {
//                // Resize the bitmap to the required input size of tflite model
//                Bitmap resizedBitmap = Bitmap.createScaledBitmap(
//                        capturedBitmap, inputWidth, inputHeight, true);
//
//                // Convert the resized bitmap to a float array
//                float[] inputArray = convertBitmapToFloatArray(resizedBitmap);
//
//                // Make predictions using the TFLite model
//                float[] predictions = mTFLiteModelExecutor.runInference(inputArray);
//
//                // Process the predictions (e.g., find the highest confidence class)
//                int predictedClass = findPredictedClass(predictions);
//
//                // Perform actions based on the predicted class (e.g., update UI)
//                updateUIWithPrediction(predictedClass);
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "Error processing captured image: " + e.getMessage());
//        }
//    }
//
//    private float[] convertBitmapToFloatArray(Bitmap bitmap) {
//        // Convert the bitmap to a float array and normalize pixel values
//        // Normalize pixel values to [0, 1] and reshape into a 1D array
//        float[] floatArray = new float[inputWidth * inputHeight * 3];
//        int index = 0;
//
//        for (int y = 0; y < inputHeight; y++) {
//            for (int x = 0; x < inputWidth; x++) {
//                int pixel = bitmap.getPixel(x, y);
//
//                // Normalize pixel values to the range [0, 1]
//                float red = ((pixel >> 16) & 0xFF) / 255.0f;
//                float green = ((pixel >> 8) & 0xFF) / 255.0f;
//                float blue = (pixel & 0xFF) / 255.0f;
//
//                // Add normalized values to the float array
//                floatArray[index++] = red;
//                floatArray[index++] = green;
//                floatArray[index++] = blue;
//            }
//        }
//
//        return floatArray;
//    }
//
//    private int findPredictedClass(float[] predictions) {
//        // Find the index of the class with the highest prediction score
//        int predictedClass = 0;
//        float maxScore = predictions[0];
//        for (int i = 1; i < predictions.length; i++) {
//            if (predictions[i] > maxScore) {
//                predictedClass = i;
//                maxScore = predictions[i];
//            }
//        }
//        return predictedClass;
//    }
//
//    private void updateUIWithPrediction(int predictedClass) {
//        // Update the UI based on the predicted class
//        // For example, update a TextView with the predicted herb's name
//        // ...
//        Toast.makeText(getContext(), "class: "+predictedClass, Toast.LENGTH_SHORT).show();
//    }
//
//    private void startCamera() {
//        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
//
//        cameraProviderFuture.addListener(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
//
//                    CameraSelector cameraSelector = new CameraSelector.Builder()
//                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
//                            .build();
//
//                    ImageCapture imageCapture = new ImageCapture.Builder().build();
//
//                    Camera camera = cameraProvider.bindToLifecycle(
//                            (LifecycleOwner) requireContext(),
//                            cameraSelector,
//                            imageCapture);
//                } catch (Exception e) {
//                    // Handle exception
//                }
//            }
//        }, ContextCompat.getMainExecutor(requireContext()));
//    }
//
//    private void takePhoto() {
//        File photoFile = createFile();
//        ImageCapture.OutputFileOptions outputFileOptions =
//                new ImageCapture.OutputFileOptions.Builder(photoFile).build();
//
//        ImageCapture imageCapture = new ImageCapture.Builder().build();
//        imageCapture.takePicture(outputFileOptions, cameraExecutor,
//                new ImageCapture.OnImageSavedCallback() {
//                    @Override
//                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
//                        String path = photoFile.getAbsolutePath();
//                        recognitionViewModel.setCapturedImagePath(path);
//                    }
//
//                    @Override
//                    public void onError(@NonNull ImageCaptureException exception) {
//                        // Handle error
//                    }
//                });
//    }
//
//    private File createFile() {
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis());
//        File storageDir = requireActivity().getExternalFilesDir(null);
//        return new File(storageDir, "IMG_" + timeStamp + ".jpg");
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        cameraExecutor.shutdown();
//    }