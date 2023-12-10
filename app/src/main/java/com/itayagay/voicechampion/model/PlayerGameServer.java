package com.itayagay.voicechampion.model;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * במחלקה זו קיים ייצוג של השחקן לפי אלגוריתם מנהל שחקן, המשחק והפעולות הלוגיות שרק השחקן של המשחק יכול לבצע. (שליפה של נתונים), המחלקה ממומשת על ידי תבנית עיצוב התוכנה Singletone.
 * 	המחלקה יורשת מ  -  GameServerHandler.
 * 	מפני שהמחלקה יורשת מ -  GameServereHandler היא גם כן Observable המשויך ספציפית לה ומודיע על שינו שקיים ספציפית אצל השחקן.
 */

public class PlayerGameServer extends GameServerHandler {

    private static PlayerGameServer INSTANCE = null;
    private ArrayList<IDataObserver> myPlayerServerObservers;

    private boolean managerAnsweredStatus = false;

    private static int count = 0;


    private PlayerGameServer(){

        myPlayerServerObservers = new ArrayList<>();
        notifyObserversDataChanged();

    }

    public static PlayerGameServer getInstance() {

        if (INSTANCE == null) {

            INSTANCE = new PlayerGameServer();

        }
        return INSTANCE;
    }


    public void checkForQuestion() {

        if (gameServer != null) {
            GameServerHandler.myGameServer.child("question").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    GameServerHandler.question = dataSnapshot.getValue(String.class);
                    notifyObserversDataChanged();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }
    public void checkMultiplayerStatus() {

            myGameServer.child("player_online").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()&&dataSnapshot.getValue().equals("offline?")) {

                        myGameServer.child("player_online").setValue("online");

                        onServerPause = false;
count = 0;
                    } else {
                        count++;
                        if(count == 3) {
                            onServerPause = true;
                        }



                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


    }




    public static int getCount() {
        return count;
    }

    public void checkForAnswer(){

        if (gameServer != null) {
            GameServerHandler.myGameServer.child("answer").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    GameServerHandler.answer = dataSnapshot.getValue(String.class);
                    notifyObserversDataChanged();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
    public void setPlayerStatus(String status){

        myGameServer.child("player_online").setValue(status);
        GameServerHandler.playerStatus = status;

    }

    public void setPlayerAnsweredStatus(boolean status){

        myGameServer.child("playerAnswered").setValue(status);

    }

    public void resetAnsweredStatus(){


        myGameServer.child("playerAnswered").setValue(false);
        myGameServer.child("managerAnswered").setValue(false);

        managerAnsweredStatus = false;

    }

    public boolean getManagerAnsweredStatus() {
        return this.managerAnsweredStatus ;
    }



    public void checkIfManagerAnswered() {

        if (myGameServer != null) {
            GameServerHandler.myGameServer.child("managerAnswered").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists() && dataSnapshot.getValue(boolean.class).equals(true)) {

                        managerAnsweredStatus = true;


                    } else {

                        managerAnsweredStatus = false;

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }



    public void checkForSeconds(){
        if (gameServer != null) {
            GameServerHandler.myGameServer.child("secondsLeft").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                        GameServerHandler.seconds = dataSnapshot.getValue(String.class);


                    notifyObserversDataChanged();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    public String getContestantName() {

        return GameServerHandler.contestantName;
    }

    public String getContestantImage() {

        return GameServerHandler.contestantImage;
    }


    @Override
    public void registerObserver(IDataObserver userIDataObserver) {
        if(!myPlayerServerObservers.contains(userIDataObserver)) {
            myPlayerServerObservers.add(userIDataObserver);
        }
    }

    @Override
    public void removeObserver(IDataObserver userIDataObserver) {
        if(myPlayerServerObservers.contains(userIDataObserver)) {
            myPlayerServerObservers.remove(userIDataObserver);
        }
    }

    @Override
    public void notifyObserversDataChanged() {
        for (IDataObserver observer : myPlayerServerObservers) {
            observer.onDataChanged(this);
        }
    }


}
