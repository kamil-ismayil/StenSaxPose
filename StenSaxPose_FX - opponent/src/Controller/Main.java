package Controller;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("../GUI/Main.fxml"));
        primaryStage.setTitle("Sten Sax Påse");
        primaryStage.setScene(new Scene(root, 350, 360));
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);

    }

    @FXML
    public void closeWindow(){
        System.exit(0);
    }

    @FXML
    public void Login(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../GUI/Login.fxml"));
            Parent rules = fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Sten Sax Påse Login");
            stage.setScene(new Scene(rules,350,360));
            stage.show();
        } catch (IOException e){
            System.out.println("Could not load the page");
            e.printStackTrace();
        }
    }

    @FXML
    public void Createplayer(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../GUI/Createplayer.fxml"));
            Parent rules = fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Sten Sax Påse Create Player");
            stage.setScene(new Scene(rules,350,360));
            stage.show();
        } catch (IOException e){
            System.out.println("Could not load the page");
            e.printStackTrace();
        }
    }


}
