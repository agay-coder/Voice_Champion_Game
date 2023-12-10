package com.itayagay.voicechampion.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.snackbar.Snackbar;
import com.itayagay.voicechampion.ViewModel.MyDataViewModel;
import com.itayagay.voicechampion.R;
import com.itayagay.voicechampion.model.GameServerHandler;

/**
 * עמוד הבית בו ישנה האפשרות להפעיל משחק.
 */

public class HomeFragment extends Fragment{


    private static View root;
    private MyDataViewModel viewModel;
    private Button startGameBtn;
    public Animation pop_anim;
    Button cancel_btn;
    boolean gameOn = false;
    long numOfCash;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_home, container, false);
        pop_anim = AnimationUtils.loadAnimation(getContext(), R.anim.button_pop_anim);


        viewModel = ViewModelProviders.of(getActivity()).get(MyDataViewModel.class);
        viewModel.setViewModelObserver();


        viewModel.getIfManager().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

                if(aBoolean!= null && !gameOn ) {

                   ((FragmentMasterPageActivity) getActivity()).startGamePageFragment(aBoolean);
                    gameOn = true;
                }

            }
        });

        viewModel.getNumOfCashStr().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Log.e("TAG", "--onChanged--" + s);
                if(!s.equals("-1")) {

                    ((FragmentMasterPageActivity)getActivity()).setHViewCoins(s);
                    numOfCash = Integer.parseInt(s);
                }
            }
        });
        viewModel.getNumOfLossesStr().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Log.e("TAG", "--onChanged--" + s);
                if(!s.equals("-1")) {
                    ((FragmentMasterPageActivity)getActivity()).setHViewLosses(s);
                }
            }
        });
        viewModel.getNumOfWinsStr().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Log.e("TAG", "--onChanged--" + s);
                if(!s.equals("-1")) {
                    ((FragmentMasterPageActivity)getActivity()).setHViewWins(s);
                }
            }
        });

        startGameBtn = (Button) root.findViewById(R.id.startGameButton);

        startGameBtn.setVisibility(View.VISIBLE);
        startGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.gameServerHandler.reset();

                gameOn = false;

                if (numOfCash >= 25) {
                    startGameBtn.startAnimation(pop_anim);
                    startGameBtn.setVisibility(View.INVISIBLE);
                    startGameBtn.setEnabled(false);

                    final Handler handler = new Handler();

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {


                            Dialog loadingDialog = new Dialog(getContext(), android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
                            loadingDialog.setContentView(R.layout.loading_game_dialog);
                            loadingDialog.setCancelable(false);
                            loadingDialog.setCanceledOnTouchOutside(false);
                            loadingDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);


                            loadingDialog.show();

                            loadingDialog.getWindow().getDecorView().setSystemUiVisibility(
                                    getActivity().getWindow().getDecorView().getSystemUiVisibility());

//Clear the not focusable flag from the window
                            loadingDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

                            viewModel.gameServerHandler.FindServer(loadingDialog, startGameBtn);

                            cancel_btn = (Button) loadingDialog.findViewById(R.id.cancel_btn);
                            cancel_btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    startGameBtn.setVisibility(View.VISIBLE);
                                    startGameBtn.setEnabled(true);

                                    viewModel.gameServerHandler.cancelServer();
                                    viewModel.gameServerHandler.reset();
                                    loadingDialog.dismiss();

                                }


                            });

                        }
                    }, 500);


                }else{

                    Toast.makeText(getContext(), "You cannot afford this watch an ad!", Toast.LENGTH_SHORT).show();

                }

            }
        });




        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        gameOn = false;


    }

    public void createModifySnackBar(String message) {
        Snackbar.make(null, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }



}



