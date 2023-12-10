package com.itayagay.voicechampion.model;

import androidx.annotation.NonNull;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.auth.UserInfo;

import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * במחלקה זו יש ייצוג של כל הפרופיל האישי של המשתמש ( שם, מייל, תמונה, Unique ID ואם התחבר לפני), המחלקה ממומשת על ידי תבנית עיצוב התוכנה Singleton.
 * 	המחלקה DataViewModel מקשרת את הנתונים הקיימים במחלקה זו ל  - activities.
 */

public class FirebaseUserAuthenticationHandler {

    private static FirebaseUserAuthenticationHandler INSTANCE = null;
    private FirebaseAuth mAuth;
    protected FirebaseUser firebaseUser;

    private boolean isLoggedIn = false;
    private boolean isLoggedInBefore = false;
    private GoogleSignInClient mGoogleSignInClient;


    private FirebaseUserAuthenticationHandler(){

        //setting up the Firebase Auth
        mAuth = FirebaseAuth.getInstance();

    }

    public boolean getIsLoggedIn() {
        return isLoggedIn;
    }


    public Boolean hasCurrentUser(){

        if(mAuth.getCurrentUser()!=null){

            return true;

        }
        return false;
    }

    public static FirebaseUserAuthenticationHandler getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new FirebaseUserAuthenticationHandler();
        }
        return INSTANCE;
    }


    public void logout() {

        mAuth.signOut();

        if(mGoogleSignInClient !=null){

            // Google sign out
            mGoogleSignInClient.signOut().addOnCompleteListener( new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                }
            });


        }else{
            FacebookSdk.sdkInitialize(getApplicationContext());
            new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                    .Callback() {
                @Override
                public void onCompleted(GraphResponse graphResponse) {

                    LoginManager.getInstance().logOut();

                }
            }).executeAsync();

        }


    }





    public String getUid(){

        firebaseUser = mAuth.getInstance().getCurrentUser();

        String Uid = firebaseUser.getUid();

        return Uid;

    }

    public String getName(){

        firebaseUser = mAuth.getInstance().getCurrentUser();

        return firebaseUser.getDisplayName();

    }

    public String getEmail() {

        firebaseUser = mAuth.getInstance().getCurrentUser();

        return firebaseUser.getEmail();

    }

    public String getUserPhoto() {

        String userPhoto = "";

        if(firebaseUser.getPhotoUrl()!=null) {
            for (UserInfo user : FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
                if (user.getProviderId().equals("facebook.com")) {
                    userPhoto = firebaseUser.getPhotoUrl().toString() + "/picture?type=large";
                    return userPhoto;
                }
            }

            userPhoto = firebaseUser.getPhotoUrl().toString();
        }

        return userPhoto;

    }

    public Boolean getIfUserCreatedBefore(){

       return isLoggedInBefore;

    }



    public void updateUI(boolean isNewUser,GoogleSignInClient mGoogleSignInClient) {

        mAuth = FirebaseAuth.getInstance();

        firebaseUser = mAuth.getInstance().getCurrentUser();

        isLoggedIn = true;

        isLoggedInBefore = isNewUser;

        UserStatsDatabaseHandler.getInstance();

this.mGoogleSignInClient = mGoogleSignInClient;

    }

}
