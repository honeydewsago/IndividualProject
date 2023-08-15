package com.example.pinellia;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class TFLiteModelExecutor {
    private Interpreter tflite;

    public TFLiteModelExecutor(String modelPath) {
        try {
            tflite = new Interpreter(loadModelFile(modelPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MappedByteBuffer loadModelFile(String modelPath) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(modelPath);
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = 0;
        long declaredLength = fileChannel.size();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public float[] runInference(float[] input) {
        float[][] output = new float[1][15]; // 15 classes
        tflite.run(input, output);
        return output[0];
    }
}
