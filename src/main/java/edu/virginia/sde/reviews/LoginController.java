package edu.virginia.sde.reviews;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Optional;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private UserService userService;
    private Stage primaryStage;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    public void initialize() {
        usernameField.setPromptText("Enter username");
        passwordField.setPromptText("Enter password");
        clearErrorLabel();
    }

    @FXML
    public void handleLoginButton() {
        clearErrorLabel();

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username and password cannot be empty.");
            return;
        }

        User user = new User(username, password);

        if (userService.authenticateUser(user)) {
            switchToCourseSearch();
        } else {
            errorLabel.setText("Invalid username or password.");
        }
    }

    @FXML
    public void handleCreateUserButton() {
        clearErrorLabel();

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username and password cannot be empty.");
            return;
        }

        User user = new User(username, password);

        try {
            Optional<String> result = userService.createUser(user);
            if (result.isPresent()) {
                errorLabel.setText(result.get());
                // In the UserService, createUser returns an empty Optional if there is no error creating a user.
            } else {
                errorLabel.setText("User created successfully. Please log in.");
            }
        } catch (RuntimeException e) {
            errorLabel.setText("An error occurred while creating the user. Please try again later.");
        }
    }

    private void clearErrorLabel() {
        errorLabel.setText("");
    }

    private void switchToCourseSearch() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/CourseSearch.fxml"));
            Scene scene = new Scene(loader.load());
            CourseSearchController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            controller.setUserService(userService);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            errorLabel.setText("Failed to load the Course Search screen.");
        }
    }
}
