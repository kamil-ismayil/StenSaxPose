package Controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.naming.Binding;
import javax.xml.transform.Result;
import java.io.IOException;
import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;

public class Listofplayers {

    @FXML
    private Button btnExit, btnSend;

    @FXML
    private ListView playersList;

    int currentPlayerId, currentInvitationId;
    String currentPlayerName, opponentPlayerName;
    Boolean check = false;
    Timer timer = new Timer(true);

    public void initialize(int playerId, String playerName){
        this.currentPlayerId = playerId;
        this.currentPlayerName = playerName;

        showOnlinePlayers();
        receivedInvitations();

        Stage stage = (Stage) btnExit.getScene().getWindow();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                logoutFromDB(currentPlayerId);
            }
        });
    }

    public void receivedInvitations(){ //for opponent
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ssp", "ssp_rw", "root");
                    Class.forName("org.postgresql.Driver");
                    connection.setAutoCommit(false);

                    String queryReceivedInvitation = "select \"From\",\"Id\" from \"public\".\"Invitation\" where \"To\"=? and \"Accepted\"=?;";
                    PreparedStatement stmt = connection.prepareStatement(queryReceivedInvitation);

                    stmt.setString(1, currentPlayerName);
                    stmt.setBoolean(2, false);
                    ResultSet receivedInvitationFrom = stmt.executeQuery();

                    if((receivedInvitationFrom.next()) && (check==false)){
                        opponentPlayerName = receivedInvitationFrom.getString(1);
                        currentInvitationId = receivedInvitationFrom.getInt(2);
                        check = true;

                        Platform.runLater(()->{
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                                    "Do you want to play game?",ButtonType.NO,ButtonType.YES);
                            alert.setTitle("Sten Sax Påse invitation");
                            alert.setContentText("Do you want to play game with " + opponentPlayerName + "?");
                            alert.showAndWait();

                            try {
                                Connection connection1 = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ssp", "ssp_rw", "root");
                                Class.forName("org.postgresql.Driver");
                                connection1.setAutoCommit(false);

                                if (alert.getResult() == ButtonType.YES) { //If Yes button is clicked
                                    String query = "Update \"public\".\"Invitation\" set \"Accepted\"=? where \"To\"=?";
                                    PreparedStatement stmt2 = connection1.prepareStatement(query);
                                    stmt2.setBoolean(1, true);
                                    stmt2.setString(2, currentPlayerName);
                                    stmt2.executeUpdate();
                                    stmt2.close();
                                    connection1.commit();
                                    connection1.close();

                                    startGame();
                                    timer.cancel();
                                } else { //If no button is clicked
                                    String queryDeleteInvitation = "delete from \"public\".\"Invitation\" where \"Id\"=?;";
                                    PreparedStatement stmtDeleteInvitation = connection1.prepareStatement(queryDeleteInvitation);
                                    stmtDeleteInvitation.setInt(1, currentInvitationId);
                                    stmtDeleteInvitation.executeUpdate();
                                    stmtDeleteInvitation.close();
                                    connection1.commit();
                                    connection1.close();
                                    check = false;
                                }
                            }catch (SQLException | ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                    stmt.close();
                    connection.commit();
                    connection.close();
                }catch (SQLException | ClassNotFoundException e){
                    e.printStackTrace();
                }

            }
        };
        timer.schedule(timerTask,0,3000); // run task in every 3 second

    }

    public void startGame(){

        try {
            Stage stage1 = (Stage) btnSend.getScene().getWindow();
            stage1.close();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../GUI/Game.fxml"));
            Parent parent = fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Sten Sax Påse Game - " + currentPlayerName);
            stage.setScene(new Scene(parent,500,350));
            stage.show();
            Game game = fxmlLoader.getController();
            game.initialize(currentPlayerName, opponentPlayerName, currentPlayerId);

        } catch (IOException e){
            System.out.println("Could not load the page");
            e.printStackTrace();
        }
    }

    public void showOnlinePlayers(){
        try {
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ssp", "ssp_rw", "root");
            Class.forName("org.postgresql.Driver");
            connection.setAutoCommit(false);

            String query = "select \"PlayerName\" from \"public\".\"Status\" where \"Online\"=? and \"PlayerId\"<>?;";
            PreparedStatement stmt = connection.prepareStatement(query);

            stmt.setBoolean(1, true);
            stmt.setInt(2,currentPlayerId);
            ResultSet onlinePlayers = stmt.executeQuery();

            playersList.getItems().clear();
            while(onlinePlayers.next()){
                playersList.getItems().add(onlinePlayers.getString(1));
            }

            playersList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            playersList.getSelectionModel().selectFirst();
            stmt.close();
            connection.commit();
            connection.close();
        }catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
        }

    }

    public void closeWindow(){
        logoutFromDB(currentPlayerId);
        Stage stage = (Stage) btnExit.getScene().getWindow();
        stage.close();

    }

    public void logoutFromDB(int currentPlayerId) {
        try{
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ssp", "ssp_rw", "root");
            Class.forName("org.postgresql.Driver");
            connection.setAutoCommit(false);
            String queryLogout = "Update \"public\".\"Status\" set \"Online\"=? where \"PlayerId\"=?;";
            PreparedStatement stmtLogout = connection.prepareStatement(queryLogout);
            stmtLogout.setBoolean(1, false);
            stmtLogout.setInt(2, currentPlayerId);
            stmtLogout.executeUpdate();
            stmtLogout.close();
            connection.commit();
            connection.close();
        }catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public void sendInvitation(){
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ssp", "ssp_rw", "root");
            connection.setAutoCommit(false);

            String queryInvitation = "INSERT INTO \"public\".\"Invitation\"(\"From\", \"To\", \"Accepted\") " +
                    "VALUES(?,?,?)";
            PreparedStatement stmtInvitation = connection.prepareStatement(queryInvitation);
            stmtInvitation.setString(1, currentPlayerName);
            opponentPlayerName = playersList.getSelectionModel().getSelectedItem().toString();
            stmtInvitation.setString(2, opponentPlayerName);
            stmtInvitation.setBoolean(3, false);

            stmtInvitation.executeUpdate();
            stmtInvitation.close();
            connection.commit();
            connection.close();
            btnSend.setDisable(true);
            playersList.setDisable(true);
            invitationAnswerFromOpponent();

        }catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
        }

    }

    public void invitationAnswerFromOpponent(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ssp", "ssp_rw", "root");
                    Class.forName("org.postgresql.Driver");
                    connection.setAutoCommit(false);

                    String queryReceivedInvitation = "select \"Id\",\"To\" from \"public\".\"Invitation\" where \"From\"=? and \"Accepted\"=?;";
                    PreparedStatement stmt = connection.prepareStatement(queryReceivedInvitation);

                    stmt.setString(1, currentPlayerName);
                    stmt.setBoolean(2, true);
                    ResultSet receivedInvitationFrom = stmt.executeQuery();

                    if(receivedInvitationFrom.next()){ //if the opponent has accepted the invitation
                        currentInvitationId = receivedInvitationFrom.getInt(1);

                        Platform.runLater(()->{
                            try {
                                Connection connection1 = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ssp", "ssp_rw", "root");
                                Class.forName("org.postgresql.Driver");
                                connection1.setAutoCommit(false);

                                String queryDeleteInvitation = "delete from \"public\".\"Invitation\" where \"Id\"=?;";
                                PreparedStatement stmtDeleteInvitation = connection1.prepareStatement(queryDeleteInvitation);

                                stmtDeleteInvitation.setInt(1, currentInvitationId);
                                stmtDeleteInvitation.executeUpdate();
                                stmtDeleteInvitation.close();
                                connection1.commit();
                                connection1.close();
                                timer.cancel();
                                startGame();

                            }catch (SQLException | ClassNotFoundException e){
                                e.printStackTrace();
                            }

                        });

                    }else{ //if the opponent has not accepted the offer
                        btnSend.setDisable(false);
                        playersList.setDisable(false);
                    }
                    stmt.close();
                    connection.commit();
                    connection.close();
                }catch (SQLException | ClassNotFoundException e){
                    e.printStackTrace();
                }

            }
        };
        timer.schedule(timerTask,0,3000); // run task in every 3 second

    }


}
