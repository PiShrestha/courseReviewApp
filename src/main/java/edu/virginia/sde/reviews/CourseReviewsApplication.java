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
            databaseConnection.createTables();
        } catch (SQLException e) {
            throw new RuntimeException("Error while building the database", e);
        }

        var userDatabase = new UserDatabase(databaseConnection);
        var courseDatabase = new CourseDatabase(databaseConnection);
        var reviewDatabase = new ReviewDatabase(databaseConnection);

        var userService = new UserService(userDatabase);
        var courseService = new CourseService(courseDatabase);
        var reviewService = new ReviewService(reviewDatabase);

        var fxmlLoader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/login.fxml"));
        var scene = new Scene(fxmlLoader.load(), 1280, 720);

        var controller = (LoginController) fxmlLoader.getController();
        controller.setPrimaryStage(primaryStage);
        controller.setServices(userService, courseService, reviewService);

        primaryStage.setTitle("Course Reviews");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
