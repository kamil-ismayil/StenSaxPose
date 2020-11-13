package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;

public class Createplayer {

    @FXML
    TextField newName, newSurname, newPlayername;

    @FXML
    PasswordField newPassword;

    @FXML
    Button btnCreateplayer;

    public void createPlayer(){
        Connection connection = null;
        Statement processSQLStatement = null;
        try{
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ssp", "ssp_rw", "root");
            connection.setAutoCommit(false);

            String query = "INSERT INTO \"public\".\"Player\"(\"Name\", \"Surname\", \"PlayerName\", \"Password\") " +
                    "VALUES(?,?,?,?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, newName.getText());
            stmt.setString(2,newSurname.getText());
            stmt.setString(3,newPlayername.getText());
            stmt.setString(4,newPassword.getText());

            stmt.executeUpdate();
            stmt.close();
            connection.commit();
            connection.close();

            Stage stage = (Stage) btnCreateplayer.getScene().getWindow();
            stage.close();

        }catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
        }

    }
}


