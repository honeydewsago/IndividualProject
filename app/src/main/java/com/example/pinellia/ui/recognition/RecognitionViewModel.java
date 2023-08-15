package com.example.pinellia.ui.recognition;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RecognitionViewModel extends ViewModel {

    private boolean cameraPermissionGranted = false;
    private String capturedImagePath;

    public boolean isCameraPermissionGranted() {
        return cameraPermissionGranted;
    }

    public void setCameraPermissionGranted(boolean granted) {
        cameraPermissionGranted = granted;
    }

    public String getCapturedImagePath() {
        return capturedImagePath;
    }

    public void setCapturedImagePath(String path) {
        capturedImagePath = path;
    }
}