package com.example.pinellia;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class TFLiteModelExecutor {
    private Interpreter tflite;

    public TFLiteModelExecutor(Context context, String modelPath) {
        try {
            Log.d("TFLiteModelExecutor", "Initializing TFLite interpreter...");
            tflite = new Interpreter(loadModelFile(context, modelPath));
            Log.d("TFLiteModelExecutor", "TFLite interpreter initialized successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("TFLiteModelExecutor", "Error initializing TFLite interpreter: " + e.getMessage());
        }
    }

    // Define the method to load the model from the assets folder
    private MappedByteBuffer loadModelFile(Context context, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
//
//    private MappedByteBuffer loadModelFile(Context context, String modelPath) throws IOException {
//        AssetManager assetManager = context.getAssets();
//        InputStream inputStream = assetManager.open(modelPath);
//
//        File modelFile = new File(context.getCacheDir(), "model.tflite");
//        FileOutputStream fileOutputStream = new FileOutputStream(modelFile);
//
//        byte[] buffer = new byte[1024];
//        int bytesRead;
//        while ((bytesRead = inputStream.read(buffer)) != -1) {
//            fileOutputStream.write(buffer, 0, bytesRead);
//        }
//
//        // Close streams
//        fileOutputStream.close();
//        inputStream.close();
//
//        // Map the model file into a MappedByteBuffer
//        return new FileInputStream(modelFile).getChannel().map(FileChannel.MapMode.READ_ONLY, 0, modelFile.length());
//    }

    public float[] runInference(float[] input) {
        float[][] output = new float[1][15]; // 15 classes
        tflite.run(input, output);
        return output[0];
    }
}
