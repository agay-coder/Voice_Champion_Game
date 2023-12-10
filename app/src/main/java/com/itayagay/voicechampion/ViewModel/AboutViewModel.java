package com.itayagay.voicechampion.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * המחלקה אחראית על המרת אובייקטים הנתונים מהמודל בצורה כזו שאפשר לנהל ולהציג אובייקטים בקלות בתצוגה.
 * היא גם כן אחראית על להעביר את טקסט השימוש במיקרופון ל - Fragment.
 */

public class AboutViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public AboutViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("what is the date?\n\n what is the time?\n\n what is your name?\n");
    }

    public LiveData<String> getText() {
        return mText;
    }
}