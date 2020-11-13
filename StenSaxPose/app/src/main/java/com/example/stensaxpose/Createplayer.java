package com.example.stensaxpose;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Createplayer extends AppCompatActivity {

    private static final String DB = "jdbc:postgresql://localhost:5432/ssp";
    private static final String USER = "ssp_rw";
    private static final String PASSWORD = "root";

    private EditText newName, newSurname, newPlayerName, newPsw;
    private Button btnCreatePlayer;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createplayer);
        setupUIViews();

        btnCreatePlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()){
                    preferences = getSharedPreferences("PlayernamePassword",MODE_PRIVATE);
                    editor = preferences.edit();

                    editor.putString("Name",newName.getText().toString());
                    editor.putString("Surname",newSurname.getText().toString());
                    editor.putString("Playername",newPlayerName.getText().toString());
                    editor.putString("Password",newPsw.getText().toString());
                    editor.commit();

                    Intent intent = new Intent(Createplayer.this,MainActivity.class);
                    //intent.putExtra(newName.getText().toString(),newSurname.getText().toString());
                    startActivity(intent);
                }
            }
        });

    }

    public static String encryptPassword(String password) throws NoSuchAlgorithmException {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(password.getBytes());
            byte[] bytes = md.digest();

            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
        return sb.toString(); //Get complete hashed password in hex format
    }

    private void writeDB() {
        Connection connection = null;

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Please add PostgreSQL JDBC Driver in your Classpath ");
            System.err.println(e.getMessage());
        }

        try {
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ssp", "ssp_rw", "root");
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            System.err.println("Connection Failed, Check console!!!");
            System.err.println(e.getMessage());
        }

        if (connection == null) {
            System.out.println("Connection Failed !");
        } else {
            System.out.println("Connection established!");
        }

        try {
            Statement processSQLStatement = connection.createStatement();
            String sql = String.format("insert into \"Player\" (\"Name\",\"Surname\",\"PlayerName\",\"Password\",\"Id\") " +
                        "VALUES('%s','%s','%s','%s',%s);", "Kamil", "Ismayil", "kisma", "kisma1", 1);
            processSQLStatement.executeUpdate(sql);
            processSQLStatement.close();
            connection.commit();
            connection.close();

        } catch (SQLException e) {
            System.err.println("Connection Failed, Check console!!!");
            System.err.println(e.getMessage());
        }


    }
    private void setupUIViews(){
        newName = (EditText) findViewById(R.id.newName);
        newSurname = (EditText) findViewById(R.id.newSurname);
        newPlayerName = (EditText) findViewById(R.id.newPlayerName);
        newPsw = (EditText) findViewById(R.id.Psw);
        btnCreatePlayer = (Button) findViewById(R.id.cteatePlayer);
    }

    private Boolean validate(){
        Boolean result=false;
        String name = newName.getText().toString();
        String surname = newSurname.getText().toString();
        String playerName = newPlayerName.getText().toString();
        String psw = newPsw.getText().toString();

        if(name.isEmpty() || surname.isEmpty() || playerName.isEmpty() || psw.isEmpty()){
            Toast.makeText(this,"Please enter all details!",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this,"Player successfully created",Toast.LENGTH_SHORT).show();
            result = true;
        }
        return result;
    }

}
