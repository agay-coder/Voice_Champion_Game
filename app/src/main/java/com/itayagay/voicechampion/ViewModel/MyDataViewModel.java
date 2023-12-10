package com.itayagay.voicechampion.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.itayagay.voicechampion.model.FirebaseUserAuthenticationHandler;
import com.itayagay.voicechampion.model.GameServerHandler;
import com.itayagay.voicechampion.model.IDataObservable;
import com.itayagay.voicechampion.model.IDataObserver;
import com.itayagay.voicechampion.model.ManagerGameServer;
import com.itayagay.voicechampion.model.PlayerGameServer;
import com.itayagay.voicechampion.model.UserStatsDatabaseHandler;

/**
 * אחראי על המרת אובייקטים הנתונים מהמודל בצורה כזו שאפשר לנהל ולהציג אובייקטים בקלות בתצוגה.
 * במחלקה זו ישנם מלא נתונים כגון (כמות כסף, ניצחונות, הפסדים, שם, תמונה, שם המתחרה, תמונת המתחרה ועוד...).
 */

public class MyDataViewModel extends ViewModel implements IDataObserver {

    private MutableLiveData<String> numOfCashStr = new MutableLiveData<String>();
    private MutableLiveData<String> numOfGamesPlayedStr = new MutableLiveData<String>();
    private MutableLiveData<String> numOfWinsStr = new MutableLiveData<String>();
    private MutableLiveData<String> numOfLossesStr = new MutableLiveData<String>();
    private MutableLiveData<String> userNameStr = new MutableLiveData<String>();
    private MutableLiveData<String> userPhotoStr = new MutableLiveData<String>();
    private MutableLiveData<String> contestantNameStr = new MutableLiveData<String>();
    private MutableLiveData<String> contestantPhotoStr = new MutableLiveData<String>();
    private MutableLiveData<String> question = new MutableLiveData<String>();
    private MutableLiveData<String> answer = new MutableLiveData<String>();
    private MutableLiveData<String> seconds = new MutableLiveData<String>();
    private MutableLiveData<Boolean> ifManager = new MutableLiveData<Boolean>();
    public UserStatsDatabaseHandler userStatsDatabaseHandler;
    public  FirebaseUserAuthenticationHandler firebaseUserAuthenticationHandler;
    public GameServerHandler gameServerHandler = new GameServerHandler();
    public  PlayerGameServer playerGameServer;
    public  ManagerGameServer managerGameServer;


    public void setViewModelObserver() {

        gameServerHandler.registerObserver(this);

       userStatsDatabaseHandler = UserStatsDatabaseHandler.getInstance();
         firebaseUserAuthenticationHandler = FirebaseUserAuthenticationHandler.getInstance();;

        userStatsDatabaseHandler.registerObserver(this);
        userStatsDatabaseHandler.notifyObserversDataChanged();

        playerGameServer =PlayerGameServer.getInstance();
        managerGameServer  = ManagerGameServer.getInstance();
        playerGameServer.registerObserver(this);

        managerGameServer.registerObserver(this);

    }


    public LiveData<String> getNumOfCashStr() {
        return numOfCashStr;
    }

    public LiveData<String> getUserNameStr() {
        return userNameStr;
    }

    public LiveData<String> getContestantNameStr() {
        return contestantNameStr;
    }

    public LiveData<String> getUserPhotoStr() {
        return userPhotoStr;
    }

    public LiveData<String> getContestantPhotoStr() {
        return contestantPhotoStr;
    }

    public LiveData<String> getAnswer() {
        return answer;
    }

    public LiveData<String> getQuestion() {
        return question;
    }

    public LiveData<String> getSeconds() {
        return seconds;
    }

    public LiveData<String> getNumOfGamesPlayedStr() {
        return numOfGamesPlayedStr;
    }

    public LiveData<String> getNumOfLossesStr() {
        return numOfLossesStr;
    }

    public LiveData<String> getNumOfWinsStr() {
        return numOfWinsStr;
    }

    public LiveData<Boolean> getIfManager() {
        return ifManager;
    }


    @Override
    public void onDataChanged(IDataObservable observable) {

        if (observable instanceof UserStatsDatabaseHandler) {

                this.numOfCashStr.setValue(Long.toString(userStatsDatabaseHandler.getCash()));
                this.numOfWinsStr.setValue(Long.toString(userStatsDatabaseHandler.getWins()));
                this.numOfLossesStr.setValue(Long.toString(userStatsDatabaseHandler.getLosses()));
               this.userNameStr.setValue(firebaseUserAuthenticationHandler.getName());
               this.userPhotoStr.setValue(firebaseUserAuthenticationHandler.getUserPhoto());


        }else if(observable instanceof GameServerHandler) {
                 if (observable instanceof PlayerGameServer) {

                     this.ifManager.setValue(false);
                this.contestantNameStr.setValue(playerGameServer.getContestantName());
                this.contestantPhotoStr.setValue(playerGameServer.getContestantImage());

                this.question.setValue(playerGameServer.getQuestion());
                this.answer.setValue(playerGameServer.getAnswer());
                this.seconds.setValue(playerGameServer.getSeconds());

            } else if(observable instanceof  ManagerGameServer) {

                     this.ifManager.setValue(true);
                     this.contestantNameStr.setValue(managerGameServer.getContestantName());
                     this.contestantPhotoStr.setValue(managerGameServer.getContestantImage());

                     this.question.setValue(managerGameServer.getQuestion());
                     this.answer.setValue(managerGameServer.getAnswer());
                     this.seconds.setValue(managerGameServer.getSeconds());
                 }



        }

    }


}