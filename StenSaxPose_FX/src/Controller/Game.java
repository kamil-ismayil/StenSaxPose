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

    String opponentName, currentPlayerName, currentPlayerChoice, opponentChoice, currentPAnswer;
    int selectedButtonId, gameNumber, roundNumber=0, currentPlayerWins=0, opponentWins=0, currentPlayerId;
    Button[] buttons= new Button[3];
    //Timer timer = new Timer(true);
    int MAX_WINS = 3;
    boolean check = false;

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
        roundNumber++;
        check = false;
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ssp", "ssp_rw", "root");
        connection.setAutoCommit(false);

        String numberOfRecords = "select count(*) from \"public\".\"Result\";";
        PreparedStatement stmt = connection.prepareStatement(numberOfRecords);
        ResultSet result = stmt.executeQuery();
        if(result.next()) { gameNumber = result.getInt(1) + 1; }
        else{  gameNumber = 1; }
        stmt.close();

        String query1 = "INSERT INTO \"public\".\"Rounds\"(\"P1_choice\",\"Result_Id\",\"Round_number\") VALUES(?,?,?)";
        PreparedStatement stmt1 = connection.prepareStatement(query1);
        stmt1.setString(1, currentPlayerChoice);
        stmt1.setInt(2, gameNumber);
        stmt1.setInt(3, roundNumber);
        stmt1.executeUpdate();
        stmt1.close();
        connection.commit();
        connection.close();
        btnPlay.setDisable(true);
        btnSten.setDisable(true);
        btnSax.setDisable(true);
        btnPose.setDisable(true);
        roundWinner();
    }

    public void roundWinner() throws SQLException, ClassNotFoundException{
        Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run(){
                Platform.runLater(()-> {
               try {
                   Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ssp", "ssp_rw", "root");
                   Class.forName("org.postgresql.Driver");
                   connection.setAutoCommit(false);

                   String queryReceivedInvitation = "select \"P2_choice\" from \"public\".\"Rounds\" where \"Result_Id\"=? and \"Round_number\"=?;";
                   PreparedStatement stmt = connection.prepareStatement(queryReceivedInvitation);
                   stmt.setInt(1, gameNumber);
                   stmt.setInt(2,roundNumber);
                   ResultSet r= stmt.executeQuery();

                       while ((r.next()) && (check==false)) { //if Player2's choice is also in the database
                           if (r.getString(1) != null) {
                               check = true;
                               btnSten.setDisable(false);
                               btnSax.setDisable(false);
                               btnPose.setDisable(false);

                               Connection connection1 = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ssp", "ssp_rw", "root");
                               Class.forName("org.postgresql.Driver");
                               connection1.setAutoCommit(false);

                               if ((currentPlayerWins < MAX_WINS) && (opponentWins < MAX_WINS)) {
                                   opponentChoice = r.getString(1);
                                   String query1 = "update \"public\".\"Rounds\" set \"Winner\"=? where \"Result_Id\"=? and \"Round_number\"=?;";
                                   PreparedStatement stmt1 = connection1.prepareStatement(query1);
                                   stmt1.setString(1, matching());
                                   stmt1.setInt(2, gameNumber);
                                   stmt1.setInt(3,roundNumber);
                                   stmt1.executeUpdate();
                                   stmt1.close();
                                   connection1.commit();
                                   //connection1.close();
                               }

                               if ((currentPlayerWins == MAX_WINS) || (opponentWins == MAX_WINS) ) {
                                   String gameWinner, gameLooser;

                                   if (currentPlayerWins > opponentWins) {
                                       gameWinner = currentPlayerName;
                                       gameLooser = opponentName;
                                       currentPStatus.setFont(Font.font("System",26));
                                       currentPStatus.setText("Winner");
                                   } else{
                                       gameWinner = opponentName;
                                       gameLooser = currentPlayerName;
                                       opponentStatus.setFont(Font.font("System",26));
                                       opponentStatus.setText("Winner");
                                   }
                                   String query2 = "INSERT INTO \"public\".\"Result\"(\"Winner\",\"Looser\") VALUES(?,?)";
                                   PreparedStatement stmt2 = connection1.prepareStatement(query2);
                                   stmt2.setString(1, gameWinner);
                                   stmt2.setString(2, gameLooser);
                                   stmt2.executeUpdate();
                                   stmt2.close();
                                   connection1.commit();
                                   //connection1.close();
                                   reMatch();
                                   check = true;
                                   timer.cancel();
                               }
                               connection1.close();
                           }
                           connection.commit();
                           connection.close();

                       }
                   connection.close();
               }catch (SQLException | ClassNotFoundException | IOException e){
                   e.printStackTrace();
               }
               });
            }
        };
        timer.schedule(timerTask,0,3000); // run task in every 3 second
    }

    public String matching(){
        String roundWinner = null;
        if(currentPlayerChoice.equals(opponentChoice)){
            roundWinner = "Draw";
        }else{
            switch (currentPlayerChoice){
                case "Sten":
                    if(opponentChoice.equals("Sax")){ roundWinner = currentPlayerName;}
                    else{ roundWinner = opponentName; }
                    break;
                case "Sax":
                    if(opponentChoice.equals("Sten")){ roundWinner = opponentName;}
                    else{ roundWinner = currentPlayerName; }
                    break;
                case "Påse":
                    if(opponentChoice.equals("Sax")){ roundWinner = opponentName;}
                    else{ roundWinner = currentPlayerName; }
                    break;
            }
        }

        if(roundWinner.equals(currentPlayerName)){
            currentPlayerWins++;
        }else if(roundWinner.equals(opponentName)){
            opponentWins++;
        }
        scoreLabel.setText(currentPlayerWins + " - " + opponentWins);

     return roundWinner;
    }

    public void reMatch() throws SQLException, ClassNotFoundException, IOException {
        String query;
        PreparedStatement stmt;
        Timer timerRematch = new Timer(true);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Would you like to have a re-match?", ButtonType.NO,ButtonType.YES);
        alert.setTitle("Sten Sax Påse invitation");
        alert.setContentText("Do you want to play game again with " + opponentName + "?");
        alert.showAndWait();

        Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ssp", "ssp_rw", "root");
        Class.forName("org.postgresql.Driver");
        connection.setAutoCommit(false);

        if (alert.getResult() == ButtonType.YES) {//If Yes button is clicked
            currentPAnswer = "Yes";
        }else{
            currentPAnswer = "No";
        }

        query = "insert into \"public\".\"Rematch\"(\"P1_answer\",\"Result_Id\") values(?,?)";
        stmt = connection.prepareStatement(query);
        stmt.setString(1, currentPAnswer);
        stmt.setInt(2, gameNumber);
        stmt.executeUpdate();
        stmt.close();
        connection.commit();

            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(()-> {
                        try {
                            Connection connection1 = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ssp", "ssp_rw", "root");
                            Class.forName("org.postgresql.Driver");
                            connection1.setAutoCommit(false);

                            String query1 = "select \"P2_answer\" from \"public\".\"Rematch\" where \"Result_Id\"=?";
                            PreparedStatement stmt1 = connection1.prepareStatement(query1);
                            stmt1.setInt(1, gameNumber);
                            ResultSet resultSet = stmt1.executeQuery();

                                    if ((resultSet.next()) && (resultSet.getString(1) != null)) {
                                        if ((resultSet.getString(1).equals("Yes")) && (currentPAnswer.equals("Yes"))){ //If the opponent replied yes to rematch

                                            roundNumber = 0;
                                            currentPlayerWins = 0;
                                            opponentWins = 0;
                                            check = false;
                                            currentPStatus.setText(" ");
                                            opponentStatus.setText(" ");
                                            scoreLabel.setText("0 - 0");

                                        } else {
                                            Platform.runLater(()-> {
                                                try {
                                                    Stage stage1 = (Stage) btnPlay.getScene().getWindow();
                                                    stage1.close();
                                                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../GUI/Listofplayers.fxml"));
                                                    Parent parent = fxmlLoader.load();
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
                            connection1.close();
                        } catch (SQLException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        });
                    }
            };
            timerRematch.schedule(timerTask,0,3000);
    }

}
