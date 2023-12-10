package com.itayagay.voicechampion.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.MultiDex;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.itayagay.voicechampion.R;
import com.itayagay.voicechampion.model.FirebaseUserAuthenticationHandler;
import com.itayagay.voicechampion.recievers.NetworkChangeReceiver;

import java.util.Arrays;

/**
 * עמוד זה הוא עמוד הכניסה לאפליקציה בו למשתמש יש את האפשרת להיכנס דרך פייסבוק או גוגל, או המתמש מחובר כבר הוא מועבר ישר לעמוד הבית.
 */

public class InitialPageActivity extends AppCompatActivity {


    private Button btn_googleLogin;
    private CallbackManager callbackManager;// handle login responses
    private LoginManager loginManager;
    public Animation pop_anim;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private Button facebook_loginButton;
    TextView orTextView;
    TextView continueTextView;
    ImageView line1ImageView;
    ImageView line2ImageView;
    private static final int GOOGLE_SIGN = 198;
    FirebaseUserAuthenticationHandler firebaseUserAuthentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialization_page);
        MultiDex.install(this);
        FacebookSdk.sdkInitialize(getApplicationContext());

        registerReceiver(new NetworkChangeReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        pop_anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_pop_anim);
        btn_googleLogin = (Button) findViewById(R.id.loginGoogle);
        facebook_loginButton = (Button) findViewById(R.id.loginFacebook);
facebook_loginButton.setBackgroundResource(R.drawable.facebook_login_button);
        callbackManager = CallbackManager.Factory.create();
loginManager = LoginManager.getInstance();
        mAuth = FirebaseAuth.getInstance();

        firebaseUserAuthentication = FirebaseUserAuthenticationHandler.getInstance();

      orTextView = (TextView) findViewById(R.id.orTextView);
      continueTextView = (TextView) findViewById(R.id.continueTextView);
      line1ImageView = (ImageView) findViewById(R.id.line1ImageView);
      line2ImageView = (ImageView) findViewById(R.id.line2ImageView);

        setTheVisibility(View.VISIBLE);

        if (!firebaseUserAuthentication.hasCurrentUser()) {

            btn_googleLogin.setOnClickListener(v -> SignInGoogle());

            facebook_loginButton.setOnClickListener(v -> SignInWithFacebook());


        } else {
            firebaseUserAuthentication.updateUI(false,mGoogleSignInClient);
            Intent intent = new Intent(this, FragmentMasterPageActivity.class);
            startActivity(intent);
        }
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        FirebaseMessaging.getInstance().subscribeToTopic("allDevices");
        FirebaseInstanceId.getInstance().getInstanceId();
    }

    private void setTheVisibility(int visibility){

        btn_googleLogin.setVisibility(visibility);
        orTextView.setVisibility(visibility);
        facebook_loginButton.setVisibility(visibility);
        continueTextView.setVisibility(visibility);
        line1ImageView.setVisibility(visibility);
        line2ImageView.setVisibility(visibility);

    }




    private void SignInWithFacebook() {
        setTheVisibility(View.VISIBLE);
        facebook_loginButton.startAnimation(pop_anim);
        loginManager.logInWithReadPermissions(this, Arrays.asList("public_profile","email"));
        // Initialize Facebook Login button
       loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("tag", "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("tag", "facebook:onCancel");
                // ...
                setTheVisibility(View.VISIBLE);
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("tag", "facebook:onError", error);
                // ...
                setTheVisibility(View.VISIBLE);
            }
        });

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

    //Google sign in activity starter
    public void SignInGoogle() {


        setTheVisibility(View.INVISIBLE);
        mAuth = FirebaseAuth.getInstance();
//setting up the Google login parameters
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();
        btn_googleLogin.startAnimation(pop_anim);
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);


        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN);

    }

    //Check if the data we got from SignInGoogle method has no errors.
    // then send us with the Google account to Auth with firebase.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
                // ...

                setTheVisibility(View.VISIBLE);
            }

        }

        if(data!=null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }else{

            setTheVisibility(View.INVISIBLE);
        }
    }


    //Auth with fire base server and creating a user
    public void firebaseAuthWithGoogle(@Nullable GoogleSignInAccount account) {


        Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");

                            Intent intent = new Intent(InitialPageActivity.this, FragmentMasterPageActivity.class);
                            startActivity(intent);


                            firebaseUserAuthentication.updateUI(task.getResult().getAdditionalUserInfo().isNewUser(),mGoogleSignInClient);




                        } else {
                            // If sign in fails, display a message to the user.

                            setTheVisibility(View.VISIBLE);

                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                        }

                        // ...
                    }
                });

    }

    private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            Intent intent = new Intent(InitialPageActivity.this, FragmentMasterPageActivity.class);
                            startActivity(intent);

                            firebaseUserAuthentication.updateUI(task.getResult().getAdditionalUserInfo().isNewUser(),mGoogleSignInClient);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Toast.makeText(InitialPageActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                            setTheVisibility(View.VISIBLE);


                        }

                        // ...
                    }
                });
    }


}

