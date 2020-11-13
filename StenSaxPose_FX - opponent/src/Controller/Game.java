package Controller;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;

public class Game {

    @FXML
    Label currentPName, opponentPName, scoreLabel, currentPStatus, opponentStatus;

    @FXML
    Button btnSten, btnSax, btnPose, btnPlay;

    String opponentName, currentPlayerName, currentPlayerChoice, opponentChoice;
    String query, currentPAnswer;
    int p1Score, p2Score, selectedButtonId, gameNumber=0, roundNumber=0, currentPlayerWins=0, opponentWins=0, currentPlayerId;
    Button[] buttons= new Button[3];
    int MAX_WINS = 3;
    boolean check = false;

    Timer timer = new Timer(true);
    Timer timer1 = new Timer(true);

    public void initialize(String currentPlayerName, String opponentName, int currentPlayerId){
        this.opponentName = opponentName;
        this.currentPlayerName = currentPlayerName;
        this.currentPlayerId = currentPlayerId;
        currentPName.setText(currentPlayerName);
        opponentPName.setText(opponentName);
        scoreLabel.setText("0 - 0");

        buttons[0] = btnSten;
        buttons[1] = btnSax;
        buttons[2] = btnPose;

        for(int i=0; i<3; i++) {
            int finalI = i;
            buttons[i].setOnAction(e->{
                currentPlayerChoice = ((Button) e.getSource()).getText();
                selectedButtonId = finalI;
                btnPlay.setDisable(false);
            });
        }

        Stage stage = (Stage) btnPlay.getScene().getWindow();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {

                Listofplayers listofplayers= new Listofplayers();
                listofplayers.logoutFromDB(currentPlayerId);
            }
        });


    }

    public void Play() throws SQLException, ClassNotFoundException {
        timer = new Timer(true);
        timer1 = new Timer(true);
        check = false;
        roundNumber++;
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ssp", "ssp_rw", "root");
        connection.setAutoCommit(false);

        String numberOfRecords = "select count(*) from \"public\".\"Result\";";
        PreparedStatement stmt = connection.prepareStatement(numberOfRecords);
        ResultSet result = stmt.executeQuery();
        if (result.next()) {
            gameNumber = result.getInt(1) + 1;
        } else {
            gameNumber = 1;
        }
        stmt.close();


        btnPlay.setDisable(true);
        btnSten.setDisable(true);
        btnSax.setDisable(true);
        btnPose.setDisable(true);

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    String queryCheck = "select \"P1_choice\" from \"public\".\"Rounds\" where \"Result_Id\"=? and \"Round_number\"=?;";
                    PreparedStatement stmt2 = connection.prepareStatement(queryCheck);
                    stmt2.setInt(1, gameNumber);
                    stmt2.setInt(2, roundNumber);
                    ResultSet p1ChoiceInserted = stmt2.executeQuery();

                    if((p1ChoiceInserted.next()) && (check==false)) {
                        if (p1ChoiceInserted.getString(1) != null) { //if P1 choice has been inserted to DB
                            String queryUpdateP2Choice = "update \"public\".\"Rounds\" set \"P2_choice\"=? where \"Result_Id\"=? and \"Round_number\"=?;";
                            PreparedStatement stmt1 = connection.prepareStatement(queryUpdateP2Choice);
                            stmt1.setString(1, currentPlayerChoice);
                            stmt1.setInt(2, gameNumber);
                            stmt1.setInt(3, roundNumber);
                            stmt1.executeUpdate();
                            stmt1.close();
                            connection.commit();
                            connection.close();

                            roundWinner();
                            check = true;
                            timer1.cancel();
                        }
                    }
                    if(check == true) {
                        connection.close();
                    }

                }catch (SQLException e){
                    e.printStackTrace();
                }
            }
        };
        timer1.schedule(timerTask,0,3000); // run task in every 3 second
    }

    public void roundWinner(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run(){
                Platform.runLater(()-> {
                    try {
                        Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ssp", "ssp_rw", "root");
                        Class.forName("org.postgresql.Driver");
                        connection.setAutoCommit(false);

                        String queryReceivedInvitation = "select \"Winner\" from \"public\".\"Rounds\" where \"Result_Id\"=? and \"Round_number\"=?;";
                        PreparedStatement stmt = connection.prepareStatement(queryReceivedInvitation);
                        stmt.setInt(1, gameNumber);
                        stmt.setInt(2,roundNumber);
                        ResultSet r = stmt.executeQuery();

                        while (r.next()) { //if the Winner is published on the database
                            if(r.getString(1) != null) {
                                btnSten.setDisable(false);
                                btnSax.setDisable(false);
                                btnPose.setDisable(false);
                                if (r.getString(1).equals(currentPlayerName)) {
                                    currentPlayerWins++;
                                } else if(r.getString(1).equals(opponentName)){
                                    opponentWins++;
                                }
                                scoreLabel.setText(opponentWins + " - " + currentPlayerWins);
                                timer.cancel();

                                if((currentPlayerWins == MAX_WINS) || (opponentWins == MAX_WINS)){
                                    if (currentPlayerWins > opponentWins) {
                                        currentPStatus.setFont(Font.font("System",26));
                                        currentPStatus.setText("Winner");
                                    }else{
                                        opponentStatus.setFont(Font.font("System",26));
                                        opponentStatus.setText("Winner");
                                    }

                                    reMatch();
                                    check = true;
                                    timer.cancel();
                                }

                            }
                        }
                        stmt.close();
                        connection.commit();
                        connection.close();
                    } catch (SQLException | ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        };
        timer.schedule(timerTask,0,3000); // run task in every 3 second

    }

    public void reMatch() throws SQLException, ClassNotFoundException, IOException {

        PreparedStatement stmt;
        Timer timerRematch = new Timer(true);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Would you like to have a re-match?", ButtonType.NO,ButtonType.YES);
        alert.setTitle("Sten Sax Påse invitation");
        alert.setContentText("Do you want to play game with " + opponentName +"?");
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {//If Yes button is clicked
            currentPAnswer = "Yes";
        }else{
            currentPAnswer = "No";
        }

            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                        try {
                            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ssp", "ssp_rw", "root");
                            Class.forName("org.postgresql.Driver");
                            connection.setAutoCommit(false);

                            String query1 = "select \"P1_answer\" from \"public\".\"Rematch\" where \"Result_Id\"=?";
                            PreparedStatement stmt1 = connection.prepareStatement(query1);
                            stmt1.setInt(1, gameNumber);
                            ResultSet resultSet = stmt1.executeQuery();

                            if(resultSet.next()) {
                                String r1= resultSet.getString(1);

                                String query = "update \"public\".\"Rematch\" set \"P2_answer\"=? where \"Result_Id\"=? ";
                                PreparedStatement stmt = connection.prepareStatement(query);
                                stmt.setString(1, currentPAnswer);
                                stmt.setInt(2, gameNumber);
                                stmt.executeUpdate();
                                stmt.close();
                                connection.commit();
                                //connection.close();

                                if ((r1.equals("Yes")) && (alert.getResult() == ButtonType.YES)) { //If the opponent replied yes to rematch
                                    Platform.runLater(()-> {

                                        roundNumber = 0;
                                        currentPlayerWins = 0;
                                        opponentWins = 0;
                                        check = false;
                                        currentPStatus.setText(" ");
                                        opponentStatus.setText(" ");
                                        scoreLabel.setText("0 - 0");
                                    });

                                }else{
                                    Platform.runLater(()-> {
                                        try {
                                        Stage stage1 = (Stage) btnPlay.getScene().getWindow();
                                        stage1.close();
                                        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../GUI/Listofplayers.fxml"));
                                        Parent parent = null;
                                        parent = fxmlLoader.load();
                                        Stage stage = new Stage();
                                        stage.initModality(Modality.APPLICATION_MODAL);
                                        stage.setTitle("Sten Sax Påse Game - " + currentPlayerName);
                                        stage.setScene(new Scene(parent, 500, 350));
                                        stage.show();
                                        Listofplayers listofplayers = fxmlLoader.getController();
                                        listofplayers.initialize(currentPlayerId, currentPlayerName);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    });
                                }
                                timerRematch.cancel();
                            }
                            connection.close();

                        } catch (SQLException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                }
            };
            timerRematch.schedule(timerTask,0,3000);


    }


}
