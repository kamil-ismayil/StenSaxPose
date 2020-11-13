package com.example.stensaxpose;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends AppCompatActivity {

    private Button btnLogin;
    private EditText Psw, playerNamee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPref = getSharedPreferences("PlayernamePassword",MODE_PRIVATE);
        final String playerName = sharedPref.getString("Playername",null);
        final String password = sharedPref.getString("Password",null);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupUIViews();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(playerNamee.getText().toString().equals(playerName) && Psw.getText().toString().equals(password) ){
                    Intent intent = new Intent(Login.this,Game.class);
                    Toast.makeText(Login.this, "Login is successfull!", Toast.LENGTH_SHORT).show();

                    startActivity(intent);
                }else{
                    Toast.makeText(Login.this, "Playername or password not correct!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupUIViews(){
        btnLogin = (Button) findViewById(R.id.btnLoginn);
        Psw = (EditText) findViewById(R.id.Psw);
        playerNamee = (EditText) findViewById(R.id.playerName);

    }

}
