package edu.virginia.sde.reviews;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.SQLException;

public class CourseReviewsApplication extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        var databaseConnection = new DatabaseConnection("appDatabase.db");
        databaseConnection.connect();

        try {
            databaseConnection.connect();
            databaseConnection.createTables();
        } catch (SQLException e) {
            throw new RuntimeException("Error while building the database", e);
        }

        var fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
        var scene = new Scene(fxmlLoader.load());

        var controller = (LoginController) fxmlLoader.getController();
        controller.setPrimaryStage(primaryStage);

        primaryStage.setTitle("Course Reviews");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
