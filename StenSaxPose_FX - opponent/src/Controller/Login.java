package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class Login {

    @FXML
    TextField playerName;

    @FXML
    PasswordField playerPassword;

    @FXML
    Button btnLogin;

    FXMLLoader fxmlLoader;
    int playerIdDB;
    String playerNameDB;

    public void Login(){
        if((!playerName.getText().equals("")) && (!playerPassword.getText().equals(""))){
            if(LoginCheck() == true){
                try {
                    Stage stage1 = (Stage) btnLogin.getScene().getWindow();
                    stage1.close();

                    fxmlLoader = new FXMLLoader(getClass().getResource("../GUI/Listofplayers.fxml"));
                    Parent parent = fxmlLoader.load();
                    Stage stage = new Stage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setTitle("Sten Sax Påse - Loged in " + playerNameDB);
                    stage.setScene(new Scene(parent,350,360));
                    stage.show();
                    Listofplayers list = fxmlLoader.getController();
                    list.initialize(playerIdDB, playerNameDB);

                } catch (IOException e){
                    System.out.println("Could not load the page");
                    e.printStackTrace();
                }
            }

        }
    }

    public boolean LoginCheck(){
        Connection connection = null;
        Statement processSQLStatement = null;
        boolean check = false;
//        String playerNameDB;
        try{
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ssp", "ssp_rw", "root");
            connection.setAutoCommit(false);

            String query = "select \"Id\",\"PlayerName\" from \"public\".\"Player\" where \"PlayerName\"=? and \"Password\"=?;";
            PreparedStatement stmt = connection.prepareStatement(query);

            stmt.setString(1, playerName.getText());
            stmt.setString(2,playerPassword.getText());
            ResultSet validUser = stmt.executeQuery();

            while(validUser.next()){ //if the playerId exists in Player table
                playerIdDB = validUser.getInt(1);
                playerNameDB = validUser.getString(2);
                    check = true;

                    String query2 = "select \"PlayerId\" from \"public\".\"Status\" where \"PlayerName\"=?;";
                    PreparedStatement stmt2 = connection.prepareStatement(query2);
                    stmt2.setString(1, playerNameDB);
                    ResultSet playerOnline = stmt2.executeQuery();

                    if(playerOnline.next()) { //if the player exists on Status table
                        if(playerOnline.getString(1) != null) {
                            //System.out.println("Update");
                            String queryOnline = "Update \"public\".\"Status\" set \"Online\"=? where \"PlayerId\"=?";
                            PreparedStatement stmtOnline = connection.prepareStatement(queryOnline);
                            stmtOnline.setBoolean(1, true);
                            stmtOnline.setInt(2, playerIdDB);
                            stmtOnline.executeUpdate();
                            stmtOnline.close();
                            connection.commit();
                            connection.close();
                        }
                    }else{ //if the player does not exist on Status table
                        System.out.println("New player on Status table");
                        String queryStatus = "INSERT INTO \"public\".\"Status\"(\"Online\", \"PlayerId\", \"PlayerName\") " +
                             "VALUES(?,?,?)";
                        PreparedStatement stmtStatus = connection.prepareStatement(queryStatus);
                        stmtStatus.setBoolean(1, true);
                        stmtStatus.setInt(2, playerIdDB);
                        stmtStatus.setString(3, playerNameDB);
                        stmtStatus.executeUpdate();
                        stmtStatus.close();
                        connection.commit();
                        connection.close();
                    }
                    stmt2.close();
            }
            stmt.close();
        }catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
        }
        return check;
    }


}
