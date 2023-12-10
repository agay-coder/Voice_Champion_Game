package com.itayagay.voicechampion.model;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 *  מחלקה זו היא מחלקה אבסטרקטית (לא ניתנת למימוש) המחזיקה בה נתונים הקשורים לשאלה ולתשובה.
 */

public abstract class QuestionsHandler {

    public static DatabaseReference QuestionsDB = FirebaseDatabase.getInstance().getReferenceFromUrl("https://voice-champion.firebaseio.com/questions");
    public static String question = "";
    public static String answer = "";
    public static int numOfQuestion = 67;

    private static void setupNumOfQuestion(){

        QuestionsDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numOfQuestion = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }





}
