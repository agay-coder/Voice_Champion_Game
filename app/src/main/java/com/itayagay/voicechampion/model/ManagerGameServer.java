package com.itayagay.voicechampion.model;

import android.util.Log;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itayagay.voicechampion.ui.FragmentMasterPageActivity;

import java.util.ArrayList;
import java.util.Random;

/**במחלקה זו קיים ייצוג של מנהל לפי אלגוריתם מנהל שחקן,  הפעולות הלוגיות שרק זה שמנהל את המשחק יכול לבצע. (השמה של נתונים), המחלקה ממומשת על ידי תבנית עיצוב התוכנה Singletone.
 	המחלקה יורשת מ  -  GameServerHandler.
 	מפני שהמחלקה יורשת מ -  GameServereHandler היא גם כן Observable המשויך ספציפית לה ומודיע על שינו שקיים ספציפית אצל המנהל.
*/

 public class ManagerGameServer extends GameServerHandler {

    private static ManagerGameServer INSTANCE = null;
    private ArrayList<IDataObserver> myManagerServerObservers;


    private boolean playerAnsweredStatus = false;
    private static int count = 0;


    private ManagerGameServer() {
        myManagerServerObservers = new ArrayList<>();

    }

    public static ManagerGameServer getInstance() {

        if (INSTANCE == null) {

            INSTANCE = new ManagerGameServer();

        }
        return INSTANCE;
    }


    public void processSeconds(int seconds) {
        if(isServerOn()) {

            GameServerHandler.myGameServer.child("secondsLeft").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    dataSnapshot.getRef().setValue(Integer.toString(seconds));
                    GameServerHandler.seconds = Integer.toString(seconds);
                    notifyObserversDataChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }


    public void processQuestionAndAnswer() {


        DatabaseReference managerAnsweredDB = FirebaseDatabase.getInstance().getReferenceFromUrl("https://voice-champion.firebaseio.com/users/" + GameServerHandler.managerUID + "/answeredList");
        DatabaseReference playerAnsweredDB = FirebaseDatabase.getInstance().getReferenceFromUrl("https://voice-champion.firebaseio.com/users/" + GameServerHandler.contestantUID + "/answeredList");
        managerAnsweredDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot managerDataSnapshot) {
                playerAnsweredDB.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot playerDataSnapshot) {

                        for (int questionNumber = 0; questionNumber < QuestionsHandler.numOfQuestion; questionNumber++) {

                            if (!managerDataSnapshot.getValue(String.class).contains(Integer.toString(questionNumber))&&!playerDataSnapshot.getValue(String.class).contains(Integer.toString(questionNumber))) {

                                playerAnsweredDB.setValue(playerDataSnapshot.getValue(String.class)+questionNumber);
                                managerAnsweredDB.setValue(managerDataSnapshot.getValue(String.class)+questionNumber);


                                    QuestionsHandler.QuestionsDB.child(Integer.toString(questionNumber)).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            QuestionsHandler.question = dataSnapshot.child("question").getValue(String.class);
                                            QuestionsHandler.answer = dataSnapshot.child("answer").getValue(String.class);


                                            question = QuestionsHandler.question;
                                            answer = QuestionsHandler.answer;
                                            myGameServer.child("question").setValue(question);
                                            myGameServer.child("answer").setValue(answer);

                                            notifyObserversDataChanged();

                                        }


                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                break;

                                }

                            }
                        }


                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void checkMultiplayerStatus(){

    myGameServer.child("player_online").addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            if (dataSnapshot.exists() && dataSnapshot.getValue().equals("online")) {

                myGameServer.child("player_online").setValue("offline?");
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




    public void setManagerAnsweredStatus(boolean status){


        myGameServer.child("managerAnswered").setValue(status);

    }

    public static int getCount() {
        return count;
    }



    public boolean getPlayerAnsweredStatus() {
        return playerAnsweredStatus;
    }


    public void resetAnsweredStatus(){


        myGameServer.child("playerAnswered").setValue(false);
        myGameServer.child("managerAnswered").setValue(false);

        playerAnsweredStatus = false;

    }
    public void checkIfPlayerAnswered() {

        GameServerHandler.myGameServer.child("playerAnswered").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() && dataSnapshot.getValue(boolean.class).equals(true)) {

                    playerAnsweredStatus = true;

                }else{

                    playerAnsweredStatus = false;

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void checkIfPlayerOnline(){


        GameServerHandler.myGameServer.child("player_online").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                if(dataSnapshot.exists()&&dataSnapshot.getValue(String.class).equals("online")) {

                    playerStatus = "online";

                    GameServerHandler.myGameServer.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            GameServerHandler.contestantImage = dataSnapshot.child("playerImage").getValue(String.class);
                            GameServerHandler.contestantName = dataSnapshot.child("playerName").getValue(String.class);
                            GameServerHandler.contestantUID = dataSnapshot.child("playerUID").getValue(String.class);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }else {

                    GameServerHandler.playerStatus = "offline";

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public String getContestantName() {

        return GameServerHandler.contestantName;
    }

    public String getContestantImage() {

        return GameServerHandler.contestantImage;
    }



    @Override
    public void registerObserver(IDataObserver userIDataObserver) {
        if(!myManagerServerObservers.contains(userIDataObserver)) {
            myManagerServerObservers.add(userIDataObserver);
        }
    }

    @Override
    public void removeObserver(IDataObserver userIDataObserver) {
        if(myManagerServerObservers.contains(userIDataObserver)) {
            myManagerServerObservers.remove(userIDataObserver);
        }
    }

    @Override
    public void notifyObserversDataChanged() {
        for (IDataObserver observer : myManagerServerObservers) {
            observer.onDataChanged(this);
        }
    }


}
