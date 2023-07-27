package com.example.pinellia.ui.self_care;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SelfCareViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public SelfCareViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is self care fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}