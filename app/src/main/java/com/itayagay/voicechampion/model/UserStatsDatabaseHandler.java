package com.itayagay.voicechampion.model;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * במחלקה זו קיים ייצוג של הנתונים של המשתמש (כסף, נצחונות הפסדים ושאלות שהמשתמש ענה), המחלקה ממומשת על ידי תבנית עיצוב התוכנה Singletone.
 * 	המחלקה DataViewModel מקשרת את הנתונים הקיימים במחלקה זו ל  - activities.
 * 	במימוש המחלקה קיים שני מצבים, אחד כאשר המשתמש קיים אז מתקיים השמה של הנתונים ושתיים כאשר המשתמש לא היה קיים ואז יוצרים לו את הנתונים ההתחלתיים (כסף: 200 , נצחונות: 0 , הפסדים: 0 , מספר שאלות שענה: 0).
 * 	המחלקה extends ל -  IDataObservable ומודיעה (מעדכנת) ל -  DataViewModel על כל שינוי המתרחש בנתונים של המשתמש
 */

public class UserStatsDatabaseHandler implements IDataObservable {

    private static UserStatsDatabaseHandler INSTANCE = null;
    private FirebaseUserAuthenticationHandler firebaseUserAuthenticationHandler;
    private ArrayList<IDataObserver> myUserStatsObservers;


    private long cash;
    private long wins;
    private long losses;

    private DatabaseReference userDB;

    private UserStatsDatabaseHandler(){

        myUserStatsObservers = new ArrayList<>();

        this.cash = -1l;
        this.wins = -1l;
        this.losses = -1l;

        firebaseUserAuthenticationHandler = FirebaseUserAuthenticationHandler.getInstance();
        userDB = FirebaseDatabase.getInstance().getReferenceFromUrl("https://voice-champion.firebaseio.com/users/" + firebaseUserAuthenticationHandler.getUid());

        if(firebaseUserAuthenticationHandler.getIfUserCreatedBefore()==true){

            userDB.child("cash").setValue(200);
            userDB.child("wins").setValue(0);
            userDB.child("losses").setValue(0);
            userDB.child("answeredList").setValue("");
            this.cash = 200l;
            this.wins = 0l;
            this.losses = 0l;
            notifyObserversDataChanged();

        }

        userDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.child("cash").exists()&&dataSnapshot.child("losses").exists()&&dataSnapshot.child("wins").exists()) {
                    cash = dataSnapshot.child("cash").getValue(Long.class).longValue();
                    wins = dataSnapshot.child("wins").getValue(Long.class).longValue();
                    losses = dataSnapshot.child("losses").getValue(Long.class).longValue();
                    notifyObserversDataChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public static UserStatsDatabaseHandler getInstance() {

        if (INSTANCE == null) {

            INSTANCE = new UserStatsDatabaseHandler();

        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE =  null;

    }

    public long getCash(){

            return cash;
    }
    public long getWins(){

            return wins;

    }
    public long getLosses(){

            return losses;
    }

    public void earnedCash(long numOfCashEarned) {

        userDB.child("cash").setValue(this.cash+numOfCashEarned);
        notifyObserversDataChanged();

    }
    public void lostCash(long numOfCashLost) {

        userDB.child("cash").setValue(this.cash-numOfCashLost);
        notifyObserversDataChanged();

    }
    public void updateWins() {

     userDB.child("wins").setValue(this.wins+1);
     notifyObserversDataChanged();

    }
    public void updateLosses() {

        userDB.child("losses").setValue(this.losses+1);
        notifyObserversDataChanged();

    }
    public long numOfGamesPlayed(){

        return this.wins + this.losses;

    }



    @Override
    public void registerObserver(IDataObserver userIDataObserver) {
        if(!myUserStatsObservers.contains(userIDataObserver)) {
            myUserStatsObservers.add(userIDataObserver);
        }
    }

    @Override
    public void removeObserver(IDataObserver userIDataObserver) {
        if(myUserStatsObservers.contains(userIDataObserver)) {
            myUserStatsObservers.remove(userIDataObserver);
        }
    }

    @Override
    public void notifyObserversDataChanged() {
        for (IDataObserver observer : myUserStatsObservers) {
            observer.onDataChanged(this);
        }
    }
}
