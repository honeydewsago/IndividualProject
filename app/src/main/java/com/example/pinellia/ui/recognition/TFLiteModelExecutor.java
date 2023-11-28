package com.example.pinellia.ui.recognition;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.util.Pair;

import com.example.pinellia.model.Herb;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class TFLiteModelExecutor {

    // Paths to the model and label files
    private static final String MODEL_PATH = "mobilenetv2_model.tflite";
    private static final String LABEL_PATH = "labels.txt";

    // TensorFlow Lite model interpreter
    private Interpreter tflite;

    // List of labels
    private List<String> labelList;

    // ByteBuffer for image data
    private ByteBuffer imageData = null;

    // Number of top results to display
    private static final int RESULTS_TO_SHOW = 15;

    // Inference batch size
    private static final int BATCH_SIZE = 1;

    // Number of color channels (RGB)
    static final int PIXEL_SIZE = 3;

    // Image width
    static final int IMG_SIZE_X = 224;
    // Image height
    static final int IMG_SIZE_Y = 224;

    // Mean value for preprocessing
    static final int IMG_MEAN = 224;
    // Standard deviation for preprocessing
    static final float IMG_STD = 224.0f;

    // Array to store image data
    private int[] intValues = new int[IMG_SIZE_X * IMG_SIZE_Y];

    // Array to store label probabilities
    private float[][] labelProbArray = null;

    private List<Map.Entry<String, Float>> finalizedLabels = new ArrayList<>();

    public TFLiteModelExecutor(Activity activity) throws IOException {
        // Create an instance of the TensorFlow Lite model interpreter
        tflite = new Interpreter(loadModelFile(activity));

        // Load the list of labels
        labelList = loadLabelList(activity);

        // Allocate memory for image data (4 bytes per pixel)
        imageData = ByteBuffer.allocateDirect(4 * BATCH_SIZE * IMG_SIZE_X * IMG_SIZE_Y * PIXEL_SIZE);
        imageData.order(ByteOrder.nativeOrder());

        // Initialize the array to store label probabilities
        labelProbArray = new float[1][labelList.size()];

        // Log a message to indicate the creation of the TFLite Model with the model path
        Log.d("tflite", "Created a TFLite Model with " + MODEL_PATH);
    }

    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        // Load the model file from assets
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_PATH);

        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();

        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private List<String> loadLabelList(Activity activity) throws IOException {
        // Read the labels from a label text file in assets
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

    // Run inference on the provided bitmap
    public List<Map.Entry<String, Float>> runInference(Bitmap bitmap) {
        if (tflite == null) {
            Log.e("tflite", "TFLite Model has not been initialized. Unable to run inference.");
        } else {
            // Convert image bitmap to byte buffer
            convertBitmapToByteBuffer(bitmap);

            // Record the prediction time
            long startTime = SystemClock.uptimeMillis();
            tflite.run(imageData, labelProbArray);

            long endTime = SystemClock.uptimeMillis();
            Log.d("tflite", "Timecost to run model inference: " + Long.toString(endTime - startTime));

            List<Map.Entry<String, Float>> topKLabels = getTopKLabels();
            finalizedLabels = finalizeActualHerbLabels(topKLabels);
        }

        return finalizedLabels;
    }

    // Convert a bitmap to a ByteBuffer
    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if (imageData == null) {
            return;
        }
        imageData.rewind();
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        // Convert the image to floating point and preprocess
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

    // Finalize actual herb labels based on a mapping
    private List<Map.Entry<String, Float>> finalizeActualHerbLabels(List<Map.Entry<String, Float>> topKLabels) {
        List<Map.Entry<String, Float>> updatedLabels = new ArrayList<>();

        // Define a mapping of herb label names to actual herb names
        Map<String, String> herbNameMapping = new HashMap<>();
        herbNameMapping.put("aiye", "Mugwort");
        herbNameMapping.put("baihe", "Lily Bulbs");
        herbNameMapping.put("chongcao", "Cordyceps");
        herbNameMapping.put("dangshen", "Codonopsis Root");
        herbNameMapping.put("fuling", "Poria Cocos");
        herbNameMapping.put("gancao", "Licorice");
        herbNameMapping.put("gouqi", "Wolfberry / Gojiberry");
        herbNameMapping.put("heshouwu", "Tuber Fleeceflower");
        herbNameMapping.put("huangbai", "Cork-Tree Bark");
        herbNameMapping.put("huangqi", "Astragalus");
        herbNameMapping.put("jinyinhua", "Japanese Honeysuckle");
        herbNameMapping.put("luohanguo", "Monkfruit");
        herbNameMapping.put("renshen", "Ginseng");
        herbNameMapping.put("shanyao", "Chinese Yam");
        herbNameMapping.put("tiannanxing", "Chinese Arisaema");

        for (Map.Entry<String, Float> entry : topKLabels) {
            String className = entry.getKey();
            Float probability = entry.getValue();

            // Check if there is an actual herb name mapping for this class
            if (herbNameMapping.containsKey(className)) {
                String updatedLabel = herbNameMapping.get(className);
                updatedLabels.add(new AbstractMap.SimpleEntry<>(updatedLabel, probability));
            } else {
                updatedLabels.add(new AbstractMap.SimpleEntry<>(className, probability));
            }
        }

        return updatedLabels;
    }

    // Get the top K labels
    private List<Map.Entry<String, Float>> getTopKLabels() {
        List<Map.Entry<String, Float>> topKLabels = new ArrayList<>();

        for (int i = 0; i < labelList.size(); ++i) {
            // Sort the labels and probability results
            sortedLabels.add(
                    new AbstractMap.SimpleEntry<>(labelList.get(i), labelProbArray[0][i]));
            if (sortedLabels.size() > RESULTS_TO_SHOW) {
                sortedLabels.poll();
            }
        }

        while (!sortedLabels.isEmpty()) {
            topKLabels.add(sortedLabels.poll());
        }

        // Reverse the list to have the highest probabilities first
        Collections.reverse(topKLabels);

        for (Map.Entry<String, Float> entry: topKLabels) {
            // Log each entry (label and probability)
            Log.d("tflite", "Entry Label: " + entry.getKey() + ", Probability: " + entry.getValue());
        }

        return topKLabels;
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

    // Retrieve all herb data from Firebase
    public void retrieveAllHerbs(final TFLiteModelExecutor.AllHerbsCallback callback) {

        DatabaseReference herbDatabaseReference = FirebaseDatabase.getInstance().getReference("herbs");
        herbDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Herb> herbList = new ArrayList<>();

                for (DataSnapshot herbSnapshot : dataSnapshot.getChildren()) {
                    Herb herb = herbSnapshot.getValue(Herb.class);
                    herbList.add(herb);
                }

                callback.onAllHerbsRetrieved(herbList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onAllHerbsError(databaseError.toException());
            }
        });
    }

    // Callback interface for retrieving herb data
    public interface AllHerbsCallback {
        void onAllHerbsRetrieved(List<Herb> herbList);

        void onAllHerbsError(Exception e);
    }

    // Close the TFLite interpreter
    public void close() {
        tflite.close();
        tflite = null;
    }
}

