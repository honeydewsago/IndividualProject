package com.example.pinellia.ui.recognition;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class TFLiteModelExecutor {

    private static final String MODEL_PATH = "model9.tflite";
    private static final String LABEL_PATH = "labels.txt";

    private Interpreter tflite;
    private List<String> labelList;
    private ByteBuffer imageData = null;
    private static final int RESULTS_TO_SHOW = 15;
    private static final int BATCH_SIZE = 1;
    static final int PIXEL_SIZE = 3;
    static final int IMG_SIZE_X = 224;
    static final int IMG_SIZE_Y = 224;
    static final int IMG_MEAN = 224;
    static final float IMG_STD = 224.0f;
    private int[] intValues = new int[IMG_SIZE_X * IMG_SIZE_Y];
    private float[][] labelProbArray = null;

    public TFLiteModelExecutor(Activity activity) throws IOException {
        tflite = new Interpreter(loadModelFile(activity));
        labelList = loadLabelList(activity);
        imageData = ByteBuffer.allocateDirect(4 * BATCH_SIZE * IMG_SIZE_X * IMG_SIZE_Y * PIXEL_SIZE);
        imageData.order(ByteOrder.nativeOrder());
        labelProbArray = new float[1][labelList.size()];

        Log.d("tflite", "Created a TFLite Model with "+MODEL_PATH);
    }

    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_PATH);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private List<String> loadLabelList(Activity activity) throws IOException {
        List<String> labelList = new ArrayList<String>();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(activity.getAssets().open(LABEL_PATH)));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    public String runInference(Bitmap bitmap) {
        if (tflite == null) {
            Log.e("tflite", "TFLite Model has not been initialized. Unable to run inference.");
            return "Uninitialized TFLite Model";
        }

//        convertBitmapToByteBuffer(bitmap);

//        processImageForResNet50(bitmap);
        imageData = preprocessImageForResNet50(bitmap);

        long startTime = SystemClock.uptimeMillis();
        tflite.run(imageData, labelProbArray);
        long endTime = SystemClock.uptimeMillis();
        Log.d("tflite", "Timecost to run model inference: " + Long.toString(endTime - startTime));

        // print the results
        String textToShow = printTopKLabels();
        textToShow = Long.toString(endTime - startTime) + "ms" + textToShow;
        return textToShow;
    }

    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if (imageData == null) {
            return;
        }
        imageData.rewind();
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        // Convert the image to floating point.
        int pixel = 0;
        for (int i = 0; i < IMG_SIZE_X; ++i) {
            for (int j = 0; j < IMG_SIZE_Y; ++j) {
                final int val = intValues[pixel++];
                imageData.putFloat((((val >> 16) & 0xFF)- IMG_MEAN)/ IMG_STD);
                imageData.putFloat((((val >> 8) & 0xFF)- IMG_MEAN)/ IMG_STD);
                imageData.putFloat((((val) & 0xFF)- IMG_MEAN)/ IMG_STD);
            }
        }
    }

    public ByteBuffer preprocessImageForResNet50(Bitmap bitmap) {
        // Resize the image to the expected input size (IMG_SIZE_X x IMG_SIZE_Y)
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, IMG_SIZE_X, IMG_SIZE_Y, true);

        // Create a ByteBuffer to hold the processed image data
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4 * BATCH_SIZE * IMG_SIZE_X * IMG_SIZE_Y * PIXEL_SIZE);
        inputBuffer.order(ByteOrder.nativeOrder());
        inputBuffer.rewind();

        // Normalize pixel values and convert RGB to BGR
        int pixel = 0;
        for (int i = 0; i < IMG_SIZE_X; ++i) {
            for (int j = 0; j < IMG_SIZE_Y; ++j) {
                int color = resizedBitmap.getPixel(i, j);

                // Convert RGB to BGR
                float blue = ((color >> 16) & 0xFF) - 123.68f;
                float green = ((color >> 8) & 0xFF) - 116.779f;
                float red = (color & 0xFF) - 103.939f;

                // Store BGR values
                inputBuffer.putFloat(blue / 255.0f);
                inputBuffer.putFloat(green / 255.0f);
                inputBuffer.putFloat(red / 255.0f);
            }
        }

        return inputBuffer;
    }


//    public Bitmap processImageForResNet50(Bitmap bitmap) {
//        // Create a Mat object from the Bitmap
//        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC3);
//
//        // Convert the Bitmap to BGR format
//        Utils.bitmapToMat(bitmap, mat);
//        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2BGR);
//
//        // Zero-center the image with respect to the ImageNet dataset
//        Scalar mean = new Scalar(104, 117, 123); // These values are specific to ImageNet
//        Core.subtract(mat, mean, mat);
//
//        // Ensure the Mat is in the correct data type (float32)
//        mat.convertTo(mat, CvType.CV_32FC3);
//
//        // Convert the Mat back to a Bitmap
//        Bitmap processedBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(mat, processedBitmap);
//
//        return processedBitmap;
//    }

    private String printTopKLabels() {
        for (int i = 0; i < labelList.size(); ++i) {
            sortedLabels.add(
                    new AbstractMap.SimpleEntry<>(labelList.get(i), labelProbArray[0][i]));
            if (sortedLabels.size() > RESULTS_TO_SHOW) {
                sortedLabels.poll();
            }
        }
        String textToShow = "";
        final int size = sortedLabels.size();
        for (int i = 0; i < size; ++i) {
            Map.Entry<String, Float> label = sortedLabels.poll();
            textToShow = String.format("\n%s: %4.2f",label.getKey(),label.getValue()) + textToShow;
        }
        return textToShow;
    }

    private PriorityQueue<Map.Entry<String, Float>> sortedLabels =
            new PriorityQueue<>(
                    RESULTS_TO_SHOW,
                    new Comparator<Map.Entry<String, Float>>() {
                        @Override
                        public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                            return (o1.getValue()).compareTo(o2.getValue());
                        }
                    });

    public void close() {
        tflite.close();
        tflite = null;
    }
}

