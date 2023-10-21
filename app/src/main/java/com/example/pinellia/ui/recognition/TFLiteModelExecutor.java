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

    private static final String MODEL_PATH = "model8.tflite";
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

    private List<Map.Entry<String, Float>> finalizedLabels = new ArrayList<>();

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

    public List<Map.Entry<String, Float>> runInference(Bitmap bitmap) {
        if (tflite == null) {
            Log.e("tflite", "TFLite Model has not been initialized. Unable to run inference.");
        } else {
            convertBitmapToByteBuffer(bitmap);

            long startTime = SystemClock.uptimeMillis();
            tflite.run(imageData, labelProbArray);

            long endTime = SystemClock.uptimeMillis();
            Log.d("tflite", "Timecost to run model inference: " + Long.toString(endTime - startTime));

            List<Map.Entry<String, Float>> topKLabels = getTopKLabels();
            finalizedLabels = finalizeActualHerbLabels(topKLabels);
        }

        return finalizedLabels;
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

    private List<Map.Entry<String, Float>> finalizeActualHerbLabels(List<Map.Entry<String, Float>> topKLabels) {
        List<Map.Entry<String, Float>> updatedLabels = new ArrayList<>();

        // Define a mapping of herb label names to actual herb names
        Map<String, String> herbNameMapping = new HashMap<>();
        herbNameMapping.put("aiye", "Mugwort");
        herbNameMapping.put("baihe", "Lily Bulbs");
        herbNameMapping.put("chongcao", "Codonopsis Root");
        herbNameMapping.put("dangshen", "Cordyceps");
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

    private List<Map.Entry<String, Float>> getTopKLabels() {
        List<Map.Entry<String, Float>> topKLabels = new ArrayList<>();

        for (int i = 0; i < labelList.size(); ++i) {
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

    public interface AllHerbsCallback {
        void onAllHerbsRetrieved(List<Herb> herbList);

        void onAllHerbsError(Exception e);
    }

    public void close() {
        tflite.close();
        tflite = null;
    }
}

