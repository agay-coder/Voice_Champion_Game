package com.itayagay.voicechampion.ui;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.MenuItem;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.itayagay.voicechampion.R;
import com.itayagay.voicechampion.model.FirebaseUserAuthenticationHandler;
import com.itayagay.voicechampion.model.UserStatsDatabaseHandler;
import com.squareup.picasso.Picasso;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * עמוד זה הוא העמוד המכיל והשולט על כל ה Fragments , הוא מכיל בתוכו גם את ה – NavigationView ואת ה – fab (המיקרופון).
 */

public class FragmentMasterPageActivity extends AppCompatActivity implements RewardedVideoAdListener {

    private FirebaseUserAuthenticationHandler firebaseUserAuthenticationHandler;
    private AppBarConfiguration mAppBarConfiguration;
    private NavigationView navigationView;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    public static Dialog noInternetDialog;

    private TextToSpeech tts;
    private SpeechRecognizer speechRecog;


    MediaPlayer mediaPlayer = null;
    public Animation pop_anim;
    FloatingActionButton fab;
    private RewardedVideoAd mRewardedVideoAd;
    View hView;
    Runnable r;

    ImageView userImageView;
    TextView userNameDisplayTextView;
    private TextView numOfWinsTextView;
    private TextView numOfLossesTextView;
    private TextView lossesTextView;
    private TextView winsTextView;
    private TextView numOfCoinsTextView;
    ImageView numOfCoinsImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (ContextCompat.checkSelfPermission(FragmentMasterPageActivity.this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(FragmentMasterPageActivity.this,
                    Manifest.permission.RECORD_AUDIO)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(FragmentMasterPageActivity.this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }


        MobileAds.initialize(this, "ca-app-pub-5662457933163151~4572424113");
        // Use an activity context to get the rewarded video instance.
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);


        loadRewardedVideoAd();

        pop_anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_pop_anim);

        firebaseUserAuthenticationHandler = FirebaseUserAuthenticationHandler.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // SETTING UP ALL THE REQUIREMENTS FOR SPEECH TO TEXT
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // createModifySnackBar(view, "Started Listening");

                fab.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));

                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                if (ContextCompat.checkSelfPermission(FragmentMasterPageActivity.this,
                        Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Permission is not granted
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(FragmentMasterPageActivity.this,
                            Manifest.permission.RECORD_AUDIO)) {
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                    } else {
                        // No explanation needed; request the permission
                        ActivityCompat.requestPermissions(FragmentMasterPageActivity.this,
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
                    speechRecog.startListening(intent);
                    new CountDownTimer(3000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            //do nothing, just let it tick
                        }

                        public void onFinish() {
                            speechRecog.stopListening();
                        }   }.start();
                }

                initializeTextToSpeech();
                initializeSpeechRecognizer();


            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_rules, R.id.nav_about)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        hView = navigationView.getHeaderView(0);

        //setting up navigation header
        userImageView = (ImageView) hView.findViewById(R.id.userImageViewInGame);
        userNameDisplayTextView = (TextView) hView.findViewById(R.id.userNameDisplayTextView);
        numOfWinsTextView = (TextView) hView.findViewById(R.id.hNumOfWinsTextView);
        numOfLossesTextView = (TextView) hView.findViewById(R.id.hNumOfLossesTextView);
        winsTextView = (TextView)hView.findViewById(R.id.hWinsTextView);
        lossesTextView = (TextView)hView.findViewById(R.id.hLossesTextView);
        numOfCoinsTextView = (TextView) hView.findViewById(R.id.hNumOfCoinsTextView);
        numOfCoinsImageView = (ImageView)hView.findViewById(R.id.hCoinsImageView);


        if (userImageView != null && firebaseUserAuthenticationHandler.getUserPhoto() != null) {
            Picasso.get().load(firebaseUserAuthenticationHandler.getUserPhoto()).into(userImageView);
        }
        userNameDisplayTextView.setText(firebaseUserAuthenticationHandler.getName());

        //setting up logout listener
        Button logout = (Button) navigationView.findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                logout.startAnimation(pop_anim);
                UserStatsDatabaseHandler.reset();
                firebaseUserAuthenticationHandler.logout();

                Intent intent = new Intent(FragmentMasterPageActivity.this, InitialPageActivity.class);
                startActivity(intent);

            }
        });





        noInternetDialog = new Dialog(this,android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        noInternetDialog.setContentView(R.layout.no_internet_connection_dialog);
        noInternetDialog.setCancelable(false);



    }

    public void setHViewCoins(String s){

        numOfCoinsTextView.setText(s);
        numOfCoinsTextView.setVisibility(View.VISIBLE);
        numOfCoinsImageView.setVisibility(View.VISIBLE);

    }

    public void setHViewWins(String wins){

     numOfWinsTextView.setText(wins);
     numOfWinsTextView.setVisibility(View.VISIBLE);
     winsTextView.setVisibility(View.VISIBLE);

    }

    public void setHViewLosses(String losses){

        numOfLossesTextView.setText(losses);
        numOfLossesTextView.setVisibility(View.VISIBLE);
        lossesTextView.setVisibility(View.VISIBLE);

    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.LAYOUT_DIRECTION_LTR);
        }
    }

    // public void createModifySnackBar(View view, String message) {
    //      Snackbar.make(view, message, Snackbar.LENGTH_LONG)
    //              .setAction("Action", null).show();
    //  }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        InitializeUserProfile();
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_reward:
                mRewardedVideoAd.show();


            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        InitializeUserProfile();
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }



    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecog = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecog.setRecognitionListener(new RecognitionListener() {
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
                    fab.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));

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

//        Handle at least four sample cases

        if (result_message.indexOf("what") != -1) {
            if (result_message.indexOf("name") != -1) {
                speak("My Name is etai agai. the creator of this app");
            } else if (result_message.indexOf("date") != -1) {

                String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

                speak(mydate.substring(0, mydate.indexOf(",") + 7));

            } else if (result_message.indexOf("time") != -1) {

                String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

                speak(mydate.substring(mydate.indexOf(",") + 7, mydate.length() - 1));

            }
        } else if (result_message.indexOf("there") != -1) {
            if (result_message.indexOf("no player online") != -1) {
                speak("what can i do about it");
            }

        } else if (result_message.indexOf("play") != -1) {
            if (result_message.indexOf("favorite song") != -1) {

                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.take_what_you_want);
                mediaPlayer.start();

            } else if (result_message.indexOf("chill song") != -1) {

                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.put_your_head_on_my_shoulder);
                mediaPlayer.start();

            }
        } else if (result_message.indexOf("continue") != -1) {
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        }
    }


    private void initializeTextToSpeech() {
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
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

    @Override
    public void onBackPressed() {
    }

    private void speak(String message) {
        if (Build.VERSION.SDK_INT >= 21) {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
        }
    }



    @Override
    public void onPause() {
        mRewardedVideoAd.pause(this);
        super.onPause();
    }



    @Override
    public void onDestroy() {
        mRewardedVideoAd.destroy(this);
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {





        mRewardedVideoAd.resume(this);
//        Reinitialize the recognizer and tts engines upon resuming from background such as after openning the browser
        initializeSpeechRecognizer();
        initializeTextToSpeech();
        super.onResume();

    }

    public void startGamePageFragment(boolean manager){


        Fragment gamePageFragment = new GamePageFragment(manager);
        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();


        // Replace whatever is in the fragment_container view with this fragment,
// and add the transaction to the back stack if needed
        transaction.add(R.id.game_page, gamePageFragment);
       transaction.addToBackStack(null);

// Commit the transaction
        transaction.commit();



    }




    public void InitializeUserProfile() {

       /* HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_home);

        TextView numOfCoinsTextView = HomeFragment.root.findViewById(R.id.numOfCoinsTextView);
        TextView numOfLoses = HomeFragment.root.findViewById(R.id.numOfLoses);
        TextView numOfWins = HomeFragment.root.findViewById(R.id.numOfWins);
        TextView numOfGamesPlayed = HomeFragment.root.findViewById(R.id.numOfGamesPlayed);

        if(numOfCoinsTextView!=null) {
            numOfCoinsTextView.setText(user.getCash());
            numOfLoses.setText(user.getLoses());
            numOfWins.setText(user.getWins());
            numOfGamesPlayed.setText(user.numOfGamesPlayed());
        }

        */

    }
    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd( "ca-app-pub-3940256099942544/5224354917",
                new AdRequest.Builder().build());
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
    }


    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {

       Toast.makeText(FragmentMasterPageActivity.this, "not loadeddddddddddddddd", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRewardedVideoAdLoaded() {

    //    Toast.makeText(FragmentMasterPageActivity.this, "loadedddddddd", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRewardedVideoAdOpened() {
    }

    @Override
    public void onRewardedVideoStarted() {
    }

    @Override
    public void onRewardedVideoCompleted() {


    }
    @Override
    public void onRewardedVideoAdClosed() {
        // Load the next rewarded video ad.
        loadRewardedVideoAd();
    }

    @Override
    public void onRewarded(com.google.android.gms.ads.reward.RewardItem rewardItem) {

        UserStatsDatabaseHandler.getInstance().earnedCash(rewardItem.getAmount()+40);

    }
}
