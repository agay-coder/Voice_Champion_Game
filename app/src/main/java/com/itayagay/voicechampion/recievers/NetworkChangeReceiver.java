package com.itayagay.voicechampion.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


import static com.itayagay.voicechampion.ui.FragmentMasterPageActivity.noInternetDialog;


/**
 * מטרת המחלקה להאזין ולדעת מתי קיים שינוי באינטרנט.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        try
        {
            if (!isOnline(context)) {

            noInternetDialog.show();

            }else{

                noInternetDialog.dismiss();

            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            //should check null because in airplane mode it will be null
            return (netInfo != null && netInfo.isConnected());
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }
}
