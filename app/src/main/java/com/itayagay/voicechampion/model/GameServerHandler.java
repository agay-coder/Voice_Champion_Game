package com.itayagay.voicechampion.model;

import android.app.Dialog;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itayagay.voicechampion.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * במחלקה זו קיים ייצוג של הנתונים של המשחק  (המשחק פועל, המנהל מחובר , המתחרה מחובר, המשחק נעצר, שאלה, תשובה  ומספר שניות) והנתונים של השחקן מולו מתחרה משתמש (שם השחקן, תמונת השחקן וה – Unique ID של השחקן).
 * 	המחלקה extends ל -  IDataObservable ומודיעה (מעדכנת) ל -  DataViewModel על כל שינוי המתרחש בנתונים של המשתמש.
 * 	ממחלקה זו יורשים שתי מחלקות ManagerGameServer ו PlayerGameServer שואבות את נתוני המשחק ומפעילות את הלוגיקות המסוימות שלהן.
 */

public class GameServerHandler implements IDataObservable {

    private FirebaseUserAuthenticationHandler firebaseUserAuthenticationHandler;
    protected static DatabaseReference gameServer;
    protected static DatabaseReference myGameServer;
    protected static boolean onServer;
    protected static Boolean manager;

    protected static String playerStatus = "offline";
    protected static boolean someoneOffline = false;


    protected  static boolean onServerPause;
    public  CountDownTimer countDownTimer;
    protected static String contestantName = "";
    protected static String contestantUID = "";
    protected static String managerUID = "";
    protected static String contestantImage = "";
    protected static String seconds = "30";

    protected static String question;
    protected static String answer;
    private UserStatsDatabaseHandler userStatsDatabaseHandler;
    private static ArrayList<IDataObserver> myGameServerObservers;


    public GameServerHandler() {

        gameServer = FirebaseDatabase.getInstance().getReferenceFromUrl("https://voice-champion.firebaseio.com/game servers");
        userStatsDatabaseHandler = UserStatsDatabaseHandler.getInstance();
        firebaseUserAuthenticationHandler = FirebaseUserAuthenticationHandler.getInstance();
        onServer = false;
        onServerPause = false;
        manager = false;
        myGameServerObservers = new ArrayList<>();

    }



    public void FindServer(Dialog loadingDialog, Button startGameBtn) {

        CircleImageView circleImageView = (CircleImageView) loadingDialog.findViewById(R.id.contestantCircleImageView);

        TextView searchingForPlayersTextView = (TextView) loadingDialog.findViewById(R.id.searchingForPlayersTextView);
        TextView foundTextView = (TextView) loadingDialog.findViewById(R.id.foundPlayerTextView);
        loadingDialog.findViewById(R.id.cancel_btn).setVisibility(View.VISIBLE);


        gameServer.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot serverSnapshot : dataSnapshot.getChildren()) {

                    if (serverSnapshot.exists() && serverSnapshot.child("player_online").exists()
                            && serverSnapshot.child("player_online").getValue(String.class).equals("offline") && !onServer) {

                        serverSnapshot.child("player_online").getRef().setValue("online");
                        serverSnapshot.child("playerName").getRef().setValue(firebaseUserAuthenticationHandler.getName());
                        serverSnapshot.child("playerImage").getRef().setValue(firebaseUserAuthenticationHandler.getUserPhoto());
                        serverSnapshot.child("playerUID").getRef().setValue(firebaseUserAuthenticationHandler.getUid());

                        myGameServer = serverSnapshot.getRef();

                        onServer = true;
                        onServerPause = false;
                        playerStatus = "online";

                        GameServerHandler.contestantImage = serverSnapshot.child("managerImage").getValue(String.class);
                        GameServerHandler.contestantName = serverSnapshot.child("managerName").getValue(String.class);
                        GameServerHandler.contestantUID = serverSnapshot.child("managerUID").getValue(String.class);


                        Picasso.get().load(contestantImage).into(circleImageView);


                        circleImageView.setVisibility(View.VISIBLE);
                        loadingDialog.findViewById(R.id.cancel_btn).setVisibility(View.INVISIBLE);

                        searchingForPlayersTextView.setVisibility(View.INVISIBLE);
                        foundTextView.setVisibility(View.VISIBLE);

                        userStatsDatabaseHandler.lostCash(25);
                        //viewModel.setNumOfCoinsStr(Long.toString(user.getCash()));

                        Handler handler = new Handler();
                        Runnable r = new Runnable() {
                            public void run() {

                                manager = false;
                                onServer = true;
                                loadingDialog.dismiss();
                                startGameBtn.setVisibility(View.VISIBLE);
                                startGameBtn.setEnabled(true);

                                notifyObserversDataChanged();
                                PlayerGameServer.getInstance().notifyObserversDataChanged();

                            }
                        };
                        handler.postDelayed(r, 5000);

                    }


                }


                if (!onServer) {

                    ManagerGameServer managerGameServer = ManagerGameServer.getInstance();

                    String myServerStr = firebaseUserAuthenticationHandler.getUid() + "_server";
                    myGameServer = gameServer.child(myServerStr);
                    myGameServer.child("managerUID").setValue(firebaseUserAuthenticationHandler.getUid());
                    myGameServer.child("playerUID").setValue("");
                    myGameServer.child("managerName").setValue(firebaseUserAuthenticationHandler.getName());
                    myGameServer.child("managerImage").setValue(firebaseUserAuthenticationHandler.getUserPhoto());
                    myGameServer.child("question").setValue("");
                    myGameServer.child("answer").setValue("");
                    myGameServer.child("managerAnswered").setValue(false);
                    myGameServer.child("playerAnswered").setValue(false);
                    myGameServer.child("playerImage").setValue("");
                    myGameServer.child("playerName").setValue("");
                    myGameServer.child("player_online").setValue("offline");
                    myGameServer.child("someoneOffline").setValue(false);
                    myGameServer.child("secondsLeft").setValue("30");


                    managerUID = firebaseUserAuthenticationHandler.getUid();

                    managerGameServer.checkIfPlayerOnline();

                     countDownTimer = new CountDownTimer(3000000, 1000) {
                        public void onTick(long millisecondUntilDone) {
                            //Countdown is counting down(every second)

                          if(GameServerHandler.playerStatus.equals("online")) {

                              loadingDialog.findViewById(R.id.cancel_btn).setVisibility(View.INVISIBLE);

                              someoneOffline = false;
                              onServer = true;

                              if(!contestantImage.isEmpty()) {
                                  Picasso.get().load(contestantImage).into(circleImageView);
                              }
                              circleImageView.setVisibility(View.VISIBLE);

                              searchingForPlayersTextView.setVisibility(View.INVISIBLE);
                              foundTextView.setVisibility(View.VISIBLE);

                              userStatsDatabaseHandler.lostCash(25);

                              Handler handler = new Handler();
                              Runnable r = new Runnable() {
                                  public void run() {

                                      manager = true;
                                      onServer = true;
                                      loadingDialog.dismiss();
                                      startGameBtn.setVisibility(View.VISIBLE);
                                      startGameBtn.setEnabled(true);

                                      notifyObserversDataChanged();
                                      managerGameServer.notifyObserversDataChanged();

                                  }
                              };
                              handler.postDelayed(r, 5000);
                              cancel();
                          }

                        }

                        public void onFinish() {
                            //counter is finished! (after 30 second)
                            startGameBtn.setVisibility(View.VISIBLE);
                            startGameBtn.setEnabled(true);

                            loadingDialog.dismiss();
                            managerGameServer.deleteServer();
                            onServer = false;

                        }

                    }.start();


                }

            }





            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });


    }

    public static void reset() {
        onServer = false;
        onServerPause = false;
        manager = null;

        playerStatus = "offline";
        someoneOffline = false;


        managerUID = "";
        contestantName = "";
        contestantUID = "";
        contestantImage = "";
        seconds = "30";

    }

    public void checkIfSomeoneOffline(){

        if(myGameServer !=null){

            myGameServer.child("someoneOffline").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.getValue(boolean.class).equals(true)) {

                        GameServerHandler.someoneOffline = true;

                    } else {

                        GameServerHandler.someoneOffline = false;

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }


    public void deleteServer(){

        GameServerHandler.myGameServer.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                Log.i("Done!","Countdown Timer is finished");
            }
        });

    }

    public boolean isServerOn() {

        return (myGameServer!=null);

    }

    public static boolean isOnServerPause() {
        return onServerPause;
    }

    public static boolean isSomeoneOffline() {
        return someoneOffline;
    }

    public String getPlayerStatus(){

        return playerStatus;

    }

    public boolean getIfManager() {

        return manager;

    }

    public boolean getServerStatus() {

        return onServer;

    }

    public String getQuestion() {

        return question;

    }

    public String getAnswer() {
        return answer;
    }

    public void cancelServer() {

        if(countDownTimer !=null){

            countDownTimer.onFinish();

        }
        if(myGameServer!=null) {
            GameServerHandler.myGameServer.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    onServer = false;

                }
            });
        }
    }

    public static void setSomeoneOffline(boolean someoneOffline) {
        GameServerHandler.someoneOffline = someoneOffline;
        myGameServer.child("someoneOffline").setValue(true);
    }

    public String getSeconds() {
        return seconds;
    }


    @Override
    public void registerObserver(IDataObserver userIDataObserver) {
        if (!myGameServerObservers.contains(userIDataObserver)) {
            myGameServerObservers.add(userIDataObserver);
        }
    }

    @Override
    public void removeObserver(IDataObserver userIDataObserver) {
        if (myGameServerObservers.contains(userIDataObserver)) {
            myGameServerObservers.remove(userIDataObserver);
        }
    }

    @Override
    public void notifyObserversDataChanged() {
            for (IDataObserver observer : myGameServerObservers) {
                observer.onDataChanged(this);
        }
    }
}
