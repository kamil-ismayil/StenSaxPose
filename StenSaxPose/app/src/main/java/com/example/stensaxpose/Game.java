package com.example.stensaxpose;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Random;

public class Game extends AppCompatActivity {
    private Button btnHuman, btnExit, btnPlay, btnResult;
    private Button HumanSten, HumanSax, HumanPose;
    private Button CPUSten, CPUSax, CPUPose;
    private String Playerchoice_result = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        setupUIViews();

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
            }
        });
    }

    private void setupUIViews(){
        SharedPreferences sharedPref = getSharedPreferences("PlayernamePassword",MODE_PRIVATE);
        final String playerName = sharedPref.getString("Playername",null);

        btnHuman = findViewById(R.id.btnHuman);
        btnExit = findViewById(R.id.btnExit);
        btnHuman.setText(playerName);
        btnPlay = findViewById(R.id.btnPlay);
        btnResult = findViewById(R.id.btnResult);

        HumanSten = findViewById(R.id.HumanSten);
        HumanSax = findViewById(R.id.HumanSax);
        HumanPose = findViewById(R.id.HumanPose);

        CPUSten = findViewById(R.id.CPUSten);
        CPUSax = findViewById(R.id.CPUSax);
        CPUPose = findViewById(R.id.CPUPose);

    }

    private void Result(String CPU_choice, String Player_choice){
        String result = null;
        if(CPU_choice.equals(Player_choice)){
            result = "It is a draw";
        }else{
            switch (Player_choice){
                case "Sten":
                    if(CPU_choice.equals("Sax")){ result = "The winner is " + btnHuman.getText().toString();}
                    else{ result = "The winner is CPU"; }
                break;
                case "Sax":
                    if(CPU_choice.equals("Sten")){ result = "The winner is CPU";}
                    else{ result = "The winner is " + btnHuman.getText().toString(); }
                    break;
                case "Påse":
                    if(CPU_choice.equals("Sax")){ result = "The winner is CPU";}
                    else{ result = "The winner is " + btnHuman.getText().toString(); }
                    break;
            }
        }
        btnResult.setText(result);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showAlertDialog();
            }
        },6000);
    }

    public void CPUchoice(){
        int random = new Random().nextInt(3);
        String CPUchoice_result = null;
        switch(random){
            case 0:
                CPUSten.setBackgroundColor(getResources().getColor(R.color.gray));
                CPUSax.setBackgroundColor(getResources().getColor(R.color.defaultgray));
                CPUPose.setBackgroundColor(getResources().getColor(R.color.defaultgray));
                CPUchoice_result = "Sten";
                break;
            case 1:
                CPUSax.setBackgroundColor(getResources().getColor(R.color.gray));
                CPUSten.setBackgroundColor(getResources().getColor(R.color.defaultgray));
                CPUPose.setBackgroundColor(getResources().getColor(R.color.defaultgray));
                CPUchoice_result = "Sax";
                break;
            case 2:
                CPUPose.setBackgroundColor(getResources().getColor(R.color.gray));
                CPUSax.setBackgroundColor(getResources().getColor(R.color.defaultgray));
                CPUSten.setBackgroundColor(getResources().getColor(R.color.defaultgray));
                CPUchoice_result = "Påse";
                break;
        }
        Result(CPUchoice_result,Playerchoice_result);
    }


    public void btnPlayOnClick(View view){
        HumanSten.setEnabled(false);
        HumanSax.setEnabled(false);
        HumanPose.setEnabled(false);

        CPUchoice();
    }

    public void btnHumanStenOnClick(View view){
        HumanSten.setBackgroundColor(getResources().getColor(R.color.gray));
        HumanSax.setBackgroundColor(getResources().getColor(R.color.defaultgray));
        HumanPose.setBackgroundColor(getResources().getColor(R.color.defaultgray));

        btnPlay.setEnabled(true);
        Playerchoice_result = "Sten";
    }

    public void btnHumanSaxOnClick(View view){
        HumanSax.setBackgroundColor(getResources().getColor(R.color.gray));
        HumanSten.setBackgroundColor(getResources().getColor(R.color.defaultgray));
        HumanPose.setBackgroundColor(getResources().getColor(R.color.defaultgray));

        btnPlay.setEnabled(true);
        Playerchoice_result = "Sax";
    }

    public void btnHumanPoseOnClick(View view){
        HumanPose.setBackgroundColor(getResources().getColor(R.color.gray));
        HumanSten.setBackgroundColor(getResources().getColor(R.color.defaultgray));
        HumanSax.setBackgroundColor(getResources().getColor(R.color.defaultgray));

        btnPlay.setEnabled(true);
        Playerchoice_result = "Påse";
    }

    public void showAlertDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Sten Sax Påse");
        alertDialog.setMessage("Do you want to play again?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                startActivity(getIntent());
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAffinity();
            }
        });
        alertDialog.show();
    }
}
