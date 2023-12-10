package com.itayagay.voicechampion.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * המחלקה אחראית על המרת אובייקטים הנתונים מהמודל בצורה כזו שאפשר לנהל ולהציג אובייקטים בקלות בתצוגה.
 * היא גם כן אחראית על להעביר את טקסט החוקים ל - Fragment.
 */

public class RulesViewModel extends ViewModel {

    private MutableLiveData<String> rulesText;

    public RulesViewModel() {
        rulesText = new MutableLiveData<>();
        rulesText.setValue("There can only be two competitors, each competitor is asked one question, the competitor has the option to click on the microphone and answer the question via microphone.\n" +
                "The contestant can attempt to answer 3  times. \n" +
                "When the 3 times passed and the contestant did not answer the correct answer he is lost.\n" +
                "The fastest player that answered the correct answer wins the round and gets a point. " +
                "If a draw goes out they both get a point.\n" +
                "The game ends after 5 rounds and if a contestant answered correctly 3 questions.\n" +
                "The contestant who won the game gets 50 coins,\n" +
                "The contestant who lost the game loses 25 coins\n" +
                "and if it a draw they both get their 25 coins back.\n" +
                "You can rematch the competitor you played with.\n" +
                "You can use two accessories in the game:\n" +
                "1) A button that returns the first letter of the answer.\n" +
                "2) A button that returns half of the answer letters. (For example, the answer is \"thread\" the app will give \"th\").\n\n");
    }

    public LiveData<String> getText() {
        return rulesText;
    }
}