
package com.itayagay.voicechampion.ui;


import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.itayagay.voicechampion.R;
import com.itayagay.voicechampion.model.GameServerHandler;
import com.itayagay.voicechampion.model.ManagerGameServer;
import com.itayagay.voicechampion.model.PlayerGameServer;
import com.itayagay.voicechampion.model.UserStatsDatabaseHandler;
import com.itayagay.voicechampion.ui.FragmentMasterPageActivity;
import com.itayagay.voicechampion.ViewModel.MyDataViewModel;
import com.squareup.picasso.Picasso;
import java.util.List;
import java.util.Locale;

/**
 * עמוד זה הוא העמוד בו המשחק מתנהל, בעמוד זה יש פעולות שקורות רק למי שמנהל ויש פעולות שקורות רק למי ששחקן לפי אלגוריתם מנהל שחקן.
 */
public class GamePageFragment extends Fragment {

    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private TextToSpeech tts; // text to speech
    private SpeechRecognizer speechRecognizer; // speech recognizer

    private ImageView[] userAnswersImageView; // user answers image view
    private ImageView[] contestantAnswersImageView; // contestant answers image view

    private TextView coinsTextView; // user num of coin text view
    private TextView questionTextView; // game question text view
    private TextView seconds; // display the num of seconds text view
    private TextView userTextView;
    private TextView contestantTextView;
    private TextView hintTextView;
    private TextView showHintTextView;
    private ImageView userImageView;
    private ImageView contestantImageView;
    private static FloatingActionButton fabInGame; // microphone

    private Dialog endGameDialog;
    private String question;
    private String answer;
    private int numOfCoins;

    private boolean manager = false;

    private Handler handler;
    private Runnable r;

    public Animation pop_anim;

    private MyDataViewModel viewModel;
    private View root;
    private Snackbar snackbar;

    private Button halfAnswer;
    private Button firstLetterAnswer;
    private Button leaveGameBtn;


    public GamePageFragment(boolean isManager) {

        manager = isManager;

    }


    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        this.viewModel = ViewModelProviders.of(getActivity()).get(MyDataViewModel.class);

        root = inflater.inflate(R.layout.fragment_game_page, container, false);

        root.setClickable(true);

        snackbar = Snackbar.make(container,"waiting for player...",Snackbar.LENGTH_INDEFINITE);



        userTextView = (TextView) root.findViewById(R.id.usernameTextView);
        userImageView = (ImageView) root.findViewById(R.id.userImageViewInGame);
        hintTextView = (TextView)root.findViewById(R.id.hintTV);
        showHintTextView = (TextView)root.findViewById(R.id.showHintTV);
        viewModel.getUserNameStr().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                userTextView.setText(s);
            }
        });

        viewModel.getUserPhotoStr().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {

                Picasso.get().load(s).into(userImageView);
            }
        });

        contestantTextView = (TextView) root.findViewById(R.id.contestantNameTextView);
        contestantImageView = (ImageView) root.findViewById(R.id.contestantImageViewInGame);


        viewModel.getContestantNameStr().observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {

                contestantTextView.setText(s);

            }
        });
        viewModel.getContestantPhotoStr().observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {

                if (s!=null && !s.isEmpty()) {

                    Picasso.get().load(s).into(contestantImageView);
                }

            }
        });

        coinsTextView = (TextView) root.findViewById(R.id.numOfCoinsTextViewInGame);

        viewModel.getNumOfCashStr().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Log.e("TAG", "--onChanged--" + s);
                if (!s.equals("-1")) {

                    coinsTextView.setText(s);
                    numOfCoins = Integer.parseInt(s);

                }
            }
        });


        pop_anim = AnimationUtils.loadAnimation(getActivity(), R.anim.button_pop_anim);

        leaveGameBtn =  (Button) root.findViewById(R.id.leaveGameBtn);
        halfAnswer =   (Button)root.findViewById(R.id.halfAnswerHelp);
        firstLetterAnswer = (Button) root.findViewById(R.id.firstLetterHelp);

        questionTextView = (TextView) root.findViewById(R.id.questionTextView);

        seconds = (TextView) root.findViewById(R.id.secondsTextView);

        userAnswersImageView = new ImageView[]{(ImageView) (root.findViewById(R.id.userImg1)),
                (ImageView) (root.findViewById(R.id.userImg2)),
                (ImageView) (root.findViewById(R.id.userImg3)),
                (ImageView) (root.findViewById(R.id.userImg4)),
                (ImageView) (root.findViewById(R.id.userImg5))};

        contestantAnswersImageView = new ImageView[]{(ImageView) (root.findViewById(R.id.contestantImg1)),
                (ImageView) (root.findViewById(R.id.contestantImg2)),
                (ImageView) (root.findViewById(R.id.contestantImg3)),
                (ImageView) (root.findViewById(R.id.contestantImg4)),
                (ImageView) (root.findViewById(R.id.contestantImg5))};

        for (int i = 0; i < 5; i++) {

            userAnswersImageView[i].setImageResource(R.drawable.ic_question_mark);
            contestantAnswersImageView[i].setImageResource(R.drawable.ic_question_mark);

        }

        fabInGame = root.findViewById(R.id.fabInGame);
        fabInGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //createModifySnackBar(view, "Hearing your answer");

                fabInGame.startAnimation(pop_anim);
                fabInGame.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));

                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Permission is not granted
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            Manifest.permission.RECORD_AUDIO)) {
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                    } else {
                        // No explanation needed; request the permission
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                } else {

                    // Permission has already been granted
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    speechRecognizer.startListening(intent);
                    new CountDownTimer(3000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            //do nothing, just let it tick
                        }

                        public void onFinish() {
                            speechRecognizer.stopListening();
                        }   }.start();
                }

                initializeTextToSpeech();
                initializeSpeechRecognizer();


            }
        });
        fabInGame.setEnabled(false);


        viewModel.getQuestion().observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {

                question = s;
                questionTextView.setText(question);

            }
        });

        viewModel.getAnswer().observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {

                answer = s;

            }
        });

        viewModel.getSeconds().observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {

                seconds.setText(s);
                try {
                    secondLeft = Integer.parseInt(seconds.getText().toString());
                } catch (Exception ParseException) {

                }

            }
        });


     leaveGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                leaveGameBtn.startAnimation(pop_anim);

                AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Are You Sure You Want To Leave?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                leaveServer();

                            }
                        }).setNegativeButton("No", null).show();
            }
        });

        if (manager) {

            managerGameProcessor();

        } else {

            playerGameProcessor();
        }


      halfAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (numOfCoins >= 10) {
                    halfAnswer.startAnimation(pop_anim);

                    if (answer != null) {

                        viewModel.userStatsDatabaseHandler.lostCash(10);

                        if (answer.length() % 2 != 0) {

                            hintTextView.setText(answer.substring(0, answer.length() / 2 + 1));

                        } else {

                            hintTextView.setText(answer.substring(0, answer.length() / 2));

                        }
                        hintTextView.setVisibility(View.VISIBLE);
                        showHintTextView.setVisibility(View.VISIBLE);

                        halfAnswer.setEnabled(false);
                        firstLetterAnswer.setEnabled(false);

                    }

                }else{

                    Toast.makeText(getContext(), "You cannot afford this!", Toast.LENGTH_SHORT).show();

                }
            }
        });

        firstLetterAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (numOfCoins >= 5) {
                    firstLetterAnswer.startAnimation(pop_anim);

                    if (answer != null) {

                        viewModel.userStatsDatabaseHandler.lostCash(5);
                        hintTextView.setText(Character.toString(answer.charAt(0)));
                        hintTextView.setVisibility(View.VISIBLE);
                        showHintTextView.setVisibility(View.VISIBLE);

                        firstLetterAnswer.setEnabled(false);

                    }

                }else{

                    Toast.makeText(getContext(), "You cannot afford this!", Toast.LENGTH_SHORT).show();

                }
            }
        });

         endGameDialog = new Dialog(getContext(),android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        endGameDialog.setContentView(R.layout.end_game_dialog);
        endGameDialog.setCanceledOnTouchOutside(true);


        return root;

    }



    public void managerGameEnded(int managerPoints, int playerPoints) {



        TextView userTextViewEndGame = (TextView) endGameDialog.findViewById(R.id.userTextViewEndGame);
        TextView contestantTextViewEndGame = (TextView) endGameDialog.findViewById(R.id.contestantTextViewEndGame);
        ImageView userImageViewEndGame = (ImageView) endGameDialog.findViewById(R.id.userImageViewEndGame);
        ImageView contestantImageViewEndGame = (ImageView) endGameDialog.findViewById(R.id.contestantImageViewEndGame);


        viewModel.getUserNameStr().observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {

                userTextViewEndGame.setText(s);

            }
        });
        viewModel.getContestantNameStr().observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {

                contestantTextViewEndGame.setText(s);

            }
        });

        viewModel.getUserPhotoStr().observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (userImageViewEndGame != null)
                    Picasso.get().load(s).into(userImageViewEndGame);

            }
        });

        viewModel.getContestantPhotoStr().observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {

                if(!s.isEmpty())
                  Picasso.get().load(s).into(contestantImageViewEndGame);

            }
        });

        if (managerPoints > playerPoints) {

            TextView winnerTextView = (TextView) endGameDialog.findViewById(R.id.winnerTextView);
            winnerTextView.setVisibility(View.VISIBLE);

            ((ImageView) endGameDialog.findViewById(R.id.userCrownImageView)).setVisibility(View.VISIBLE);
            ((ImageView) endGameDialog.findViewById(R.id.contestantCircusImageView)).setVisibility(View.VISIBLE);

            viewModel.userStatsDatabaseHandler.updateWins();
            viewModel.userStatsDatabaseHandler.earnedCash(50);


        } else if (managerPoints < playerPoints) {

            TextView lostTextView = (TextView) endGameDialog.findViewById(R.id.lostTextView);
            lostTextView.setVisibility(View.VISIBLE);

            ((ImageView) endGameDialog.findViewById(R.id.userCircusImageView)).setVisibility(View.VISIBLE);
            ((ImageView) endGameDialog.findViewById(R.id.contestantCrownImageView)).setVisibility(View.VISIBLE);

            viewModel.userStatsDatabaseHandler.updateLosses();


        } else if (managerPoints == playerPoints) {

            TextView drawTextView = (TextView) endGameDialog.findViewById(R.id.drawTextView);
            drawTextView.setVisibility(View.VISIBLE);

            ((ImageView) endGameDialog.findViewById(R.id.userCrownImageView)).setVisibility(View.VISIBLE);
            ((ImageView) endGameDialog.findViewById(R.id.contestantCrownImageView)).setVisibility(View.VISIBLE);

            viewModel.userStatsDatabaseHandler.earnedCash(25);


        }

        Button returnToHomeBtn = (Button) endGameDialog.findViewById(R.id.returnToHomeBtn);
        returnToHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                returnToHomeBtn.startAnimation(pop_anim);

                endGameDialog.dismiss();


                exit();
            }
        });
        endGameDialog.show();

    }

    public void playerGameEnded(int managerPoints, int playerPoints) {


        TextView userTextViewEndGame = (TextView) endGameDialog.findViewById(R.id.userTextViewEndGame);
        TextView contestantTextViewEndGame = (TextView) endGameDialog.findViewById(R.id.contestantTextViewEndGame);
        ImageView userImageViewEndGame = (ImageView) endGameDialog.findViewById(R.id.userImageViewEndGame);
        ImageView contestantImageViewEndGame = (ImageView) endGameDialog.findViewById(R.id.contestantImageViewEndGame);

        viewModel.getUserNameStr().observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {

                userTextViewEndGame.setText(s);

            }
        });
        viewModel.getContestantNameStr().observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {

                contestantTextViewEndGame.setText(s);

            }
        });

        viewModel.getUserPhotoStr().observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(userImageViewEndGame!=null)
                     Picasso.get().load(s).into(userImageViewEndGame);

            }
        });

        viewModel.getContestantPhotoStr().observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {

                if(!s.isEmpty()) {
                    Picasso.get().load(s).into(contestantImageViewEndGame);
                }
            }
        });


        if (managerPoints < playerPoints) {

            TextView winnerTextView = (TextView) endGameDialog.findViewById(R.id.winnerTextView);
            winnerTextView.setVisibility(View.VISIBLE);

            ((ImageView) endGameDialog.findViewById(R.id.userCrownImageView)).setVisibility(View.VISIBLE);
            ((ImageView) endGameDialog.findViewById(R.id.contestantCircusImageView)).setVisibility(View.VISIBLE);

            viewModel.userStatsDatabaseHandler.updateWins();
            viewModel.userStatsDatabaseHandler.earnedCash(50);


        } else if (managerPoints > playerPoints) {

            TextView lostTextView = (TextView) endGameDialog.findViewById(R.id.lostTextView);
            lostTextView.setVisibility(View.VISIBLE);

            ((ImageView) endGameDialog.findViewById(R.id.userCircusImageView)).setVisibility(View.VISIBLE);
            ((ImageView) endGameDialog.findViewById(R.id.contestantCrownImageView)).setVisibility(View.VISIBLE);

            viewModel.userStatsDatabaseHandler.updateLosses();


        } else if (managerPoints == playerPoints) {

            TextView drawTextView = (TextView) endGameDialog.findViewById(R.id.drawTextView);
            drawTextView.setVisibility(View.VISIBLE);

            ((ImageView) endGameDialog.findViewById(R.id.userCrownImageView)).setVisibility(View.VISIBLE);
            ((ImageView) endGameDialog.findViewById(R.id.contestantCrownImageView)).setVisibility(View.VISIBLE);

            viewModel.userStatsDatabaseHandler.earnedCash(25);


        }

        Button returnToHomeBtn = (Button) endGameDialog.findViewById(R.id.returnToHomeBtn);
        returnToHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                returnToHomeBtn.startAnimation(pop_anim);

                endGameDialog.dismiss();


                exit();
            }
        });
        endGameDialog.show();

    }

    //------------------------------------------------------------------------------------------------------------------
    //-------------------------------------------------------------------------------------------------------------------

    private boolean userAnsweredFirst = false;
    private int secondLeft = 30;

    private int managerPoints = 0;
    private int playerPoints = 0;

    private int roundNum = 1;
    int secondCount=0;
    private boolean isDraw = true;


    public void playerGameProcessor() {

        viewModel.playerGameServer.checkForQuestion();
        viewModel.playerGameServer.checkForAnswer();
        viewModel.playerGameServer.checkForSeconds();
        viewModel.playerGameServer.checkIfManagerAnswered();
        viewModel.playerGameServer.checkIfSomeoneOffline();


        handler = new Handler();
        r = new Runnable() {
            public void run() {

                if(viewModel.managerGameServer.isSomeoneOffline()){

                    leaveServer();

                }  else if ( viewModel.playerGameServer.isOnServerPause()) {


                    viewModel.playerGameServer.checkMultiplayerStatus();

                    secondCount++;

                    if(snackbar!=null && !snackbar.isShown()){
                        snackbar.show();
                        isDraw = true;
                    }

                    if(secondCount==15){

                        snackbar.dismiss();
                        playerGameEnded(0,5);

                    }

                    handler.postDelayed(r, 1000);

                } else {

                    if(snackbar!=null && snackbar.isShown()){
                        snackbar.dismiss();
                    }

                    viewModel.playerGameServer.checkMultiplayerStatus();

                    if (! viewModel.playerGameServer.isServerOn()) {

                        playerGameEnded(0, 5);

                        handler.removeCallbacks(r);

                    } else {

                        if (roundNum == 6 || managerPoints >= 3 && playerPoints < 3 || managerPoints < 3 && playerPoints >= 3) {
                            questionTextView.setText("");
                            seconds.setText("");
                            fabInGame.setEnabled(false);
                            playerGameEnded(managerPoints, playerPoints);

                        } else if ( viewModel.playerGameServer.getManagerAnsweredStatus()) {

                            Toast.makeText(getContext(), "Too Late!", Toast.LENGTH_SHORT).show();
                            userAnswersImageView[roundNum - 1].setImageResource(R.drawable.x_mark);
                            contestantAnswersImageView[roundNum - 1].setImageResource(R.drawable.ic_currect);

                            userAnsweredFirst = false;

                            managerPoints++;

                            roundNum++;

                            isDraw = false;

                            handler.postDelayed(r, 1000);

                        } else if (secondLeft > 0) {

                            fabInGame.setEnabled(true);


                            if (userAnsweredFirst) {

                                Toast.makeText(getContext(), "Got It Right!", Toast.LENGTH_SHORT).show();
                                userAnswersImageView[roundNum - 1].setImageResource(R.drawable.ic_currect);
                                contestantAnswersImageView[roundNum - 1].setImageResource(R.drawable.x_mark);

                                playerPoints++;
                                roundNum++;

                                isDraw = false;

                                secondLeft = -1;
                                userAnsweredFirst = false;


                                viewModel.playerGameServer.setPlayerAnsweredStatus(true);

                            }

                            handler.postDelayed(r, 1000);

                        } else {

                            fabInGame.setEnabled(false);

                            if (isDraw == true) {

                                Toast.makeText(getContext(), "It's A Draw!", Toast.LENGTH_SHORT).show();

                                userAnswersImageView[roundNum - 1].setImageResource(R.drawable.x_mark);
                                contestantAnswersImageView[roundNum - 1].setImageResource(R.drawable.x_mark);

                                roundNum++;


                            }
                            isDraw = true;

                            viewModel.playerGameServer.resetAnsweredStatus();

                            secondLeft = 30;
                            userAnsweredFirst = false;

                            resetHints();

                            handler.postDelayed(r, 1000);


                        }

                    }


                }
            }
        };

        handler.postDelayed(r, 3000);

    }

    public void managerGameProcessor() {

        viewModel.managerGameServer.processQuestionAndAnswer();
        viewModel.managerGameServer.processSeconds(30);
        viewModel.managerGameServer.checkIfPlayerAnswered();
        viewModel.managerGameServer.checkIfSomeoneOffline();

        fabInGame.setEnabled(true);


        handler = new Handler();
        r = new Runnable() {
            public void run() {

                if(viewModel.managerGameServer.isSomeoneOffline()){

                    leaveServer();

                }  else if (viewModel.managerGameServer.isOnServerPause()) {

                    viewModel.managerGameServer.checkMultiplayerStatus();

                    secondCount++;

                    if(snackbar!=null && !snackbar.isShown()){
                        snackbar.show();
                        isDraw = true;
                    }


                    if(secondCount==15){

                        snackbar.dismiss();
                        managerGameEnded(5,0);

                    }

                    handler.postDelayed(r, 1000);


                } else {

                    if(snackbar!=null && snackbar.isShown()){
                        snackbar.dismiss();
                    }

                    viewModel.managerGameServer.checkMultiplayerStatus();
                    if (!viewModel.managerGameServer.isServerOn()) {
                        playerGameEnded(5, 0);

                        handler.removeCallbacks(r);
                    } else {

                        if (roundNum == 6 || managerPoints >= 3 && playerPoints < 3 || managerPoints < 3 && playerPoints >= 3) {
                            questionTextView.setText("");
                            seconds.setText("");
                            fabInGame.setEnabled(false);
                            managerGameEnded(managerPoints, playerPoints);

                        } else if (secondLeft > 0) {

                            secondLeft--;
                            viewModel.managerGameServer.processSeconds(secondLeft);

                            fabInGame.setEnabled(true);

                            if (viewModel.managerGameServer.getPlayerAnsweredStatus()) {

                                Toast.makeText(getContext(), "Too Late!", Toast.LENGTH_SHORT).show();
                                userAnswersImageView[roundNum - 1].setImageResource(R.drawable.x_mark);
                                contestantAnswersImageView[roundNum - 1].setImageResource(R.drawable.ic_currect);

                                userAnsweredFirst = false;

                                isDraw = false;

                                secondLeft = 0;
                                viewModel.managerGameServer.processSeconds(secondLeft);

                                playerPoints++;

                                handler.postDelayed(r, 1000);

                            }else   if (userAnsweredFirst) {

                                Toast.makeText(getContext(), "Got It Right!", Toast.LENGTH_SHORT).show();
                                userAnswersImageView[roundNum - 1].setImageResource(R.drawable.ic_currect);
                                contestantAnswersImageView[roundNum - 1].setImageResource(R.drawable.x_mark);

                                managerPoints++;

                                isDraw = false;

                                viewModel.managerGameServer.setManagerAnsweredStatus(true);
                                secondLeft = 0;
                                viewModel.managerGameServer.processSeconds(secondLeft);


                                userAnsweredFirst = false;

                                handler.postDelayed(r, 1000);

                            }else {

                                handler.postDelayed(r, 1000);
                            }

                        } else {
                            if (isDraw == true) {

                                Toast.makeText(getContext(), "It's A Draw!", Toast.LENGTH_SHORT).show();

                                userAnswersImageView[roundNum - 1].setImageResource(R.drawable.x_mark);
                                contestantAnswersImageView[roundNum - 1].setImageResource(R.drawable.x_mark);


                            } else {
                                isDraw = true;
                            }

                            try {
                                Thread.sleep(200l);
                                secondLeft = 30;
                                viewModel.managerGameServer.processSeconds(secondLeft);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            viewModel.managerGameServer.resetAnsweredStatus();
                            viewModel.managerGameServer.processQuestionAndAnswer();


                            roundNum++;

                            resetHints();

                            handler.postDelayed(r, 1000);


                        }

                        userAnsweredFirst = false;


                    }

                }
            }

        };

        handler.postDelayed(r, 2800);
    }


    public void resetHints(){

        showHintTextView.setVisibility(View.INVISIBLE);
        hintTextView.setVisibility(View.INVISIBLE);

        Handler waitAmilli = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                halfAnswer.setEnabled(true);
                firstLetterAnswer.setEnabled(true);
            }
        },500);

    }

    //------------------------------------------------------------------------------------------------------------------
    //-------------------------------------------------------------------------------------------------------------------


    public void leaveServer() {

        if(snackbar!=null) {
            snackbar.dismiss();
        }
        handler.removeCallbacks(r);
        handler = null;
        r = null;

        if(((FragmentMasterPageActivity)getActivity())!=null) {
            if (manager) {
                    if(viewModel.managerGameServer.isServerOn()) {
                        if (viewModel.managerGameServer.isSomeoneOffline() || viewModel.managerGameServer.isOnServerPause()) {

                            managerGameEnded(5, 0);
                            viewModel.managerGameServer.deleteServer();

                        }
                    }else{


                            root.setClickable(false);
                            //  ((MasterProfilePage)getActivity()).setNavigationViewEnabled(true);
                            viewModel.managerGameServer.setSomeoneOffline(true);

                            ((FragmentMasterPageActivity) getActivity()).getSupportFragmentManager().popBackStack();


                        }




            }  else {
                if(viewModel.playerGameServer.isServerOn()) {
                if(viewModel.playerGameServer.isSomeoneOffline()|| viewModel.playerGameServer.isOnServerPause()){

                    viewModel.playerGameServer.deleteServer();
                    playerGameEnded(0,5);


                }
                }else{

                    root.setClickable(false);
                    //  ((MasterProfilePage)getActivity()).setNavigationViewEnabled(true);
                    viewModel.playerGameServer.setSomeoneOffline(true);

                    ((FragmentMasterPageActivity) getActivity()).getSupportFragmentManager().popBackStack();


                }


                }
            }
        }




    public void createModifySnackBar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public void onDestroyView() {

        snackbar.dismiss();
        snackbar = null;
        super.onDestroyView();
    }

    private void initializeTextToSpeech() {
        tts = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.UK);
                    tts.setPitch(1f);
                    tts.setSpeechRate(1f);

                }
            }
        });

    }

    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(getContext())) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {

                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {


                    fabInGame.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));

                }

                @Override
                public void onError(int error) {

                }


                @Override
                public void onResults(Bundle results) {


                    List<String> result_arr = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    processResult(result_arr.get(0));
                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
        }
    }

    private void processResult(String result_message) {
        result_message = result_message.toLowerCase();

        if (answer != null) {
            answer = answer.toLowerCase();

            if (answer.contains(" ")) {
                if (result_message.contains(" ")) {
                    if (result_message.indexOf(answer.substring(0, answer.indexOf(" ") - 1)) != -1
                            && result_message.indexOf(answer.substring(answer.indexOf(" ") + 1, answer.length() - 1)) != -1) {

                        userAnsweredFirst = true;

                    } else {

                        Toast.makeText(getContext(), "Wrong Answer Try Again!", Toast.LENGTH_SHORT).show();
                        userAnsweredFirst = false;


                    }
                } else {

                    Toast.makeText(getContext(), "Wrong Answer Try Again!", Toast.LENGTH_SHORT).show();
                    userAnsweredFirst = false;

                }

            } else {

                if (result_message.indexOf(answer) != -1) {

                    userAnsweredFirst = true;

                } else {

                    Toast.makeText(getContext(), "Wrong Answer Try Again!", Toast.LENGTH_SHORT).show();
                    userAnsweredFirst = false;

                }

            }

        }

    }




    private void speak(String message) {
        if (Build.VERSION.SDK_INT >= 21) {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        viewModel.gameServerHandler.reset();

        snackbar = null;


        super.onDestroy();
    }

    public void exit(){

        viewModel.managerGameServer.deleteServer();

        root.setClickable(false);
        //  ((MasterProfilePage)getActivity()).setNavigationViewEnabled(true);
        ((FragmentMasterPageActivity) getActivity()).getSupportFragmentManager().popBackStack();

    }


    public void onResume() {
        super.onResume();
//        Reinitialize the recognizer and tts engines upon resuming from background such as after openning the browser
        initializeSpeechRecognizer();
        initializeTextToSpeech();


    }


}
