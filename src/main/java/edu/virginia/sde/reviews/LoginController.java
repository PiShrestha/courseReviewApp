package edu.virginia.sde.reviews;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label label;

    @FXML
    private Label titleLabel;

    @FXML
    private VBox loginBox;

    @FXML
    private VBox registrationBox;

    @FXML
    private TextField registerUsernameField;

    @FXML
    private PasswordField registerPasswordField;

    @FXML
    private Label toggleLabel;

    private UserService userService;
    private CourseService courseService;
    private ReviewService reviewService;
    private Stage primaryStage;
    private boolean isLoginMode = true;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setServices(UserService userService, CourseService courseService, ReviewService reviewService) {
        this.userService = userService;
        this.courseService = courseService;
        this.reviewService = reviewService;
    }

    @FXML
    public void initialize() {
        if (label != null) {
            label.setVisible(false);
        }
    }

    @FXML
    public void handleLoginButton() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            displayLabel("Username and password cannot be empty.");
            return;
        }

        User user = new User(username, password);
        if (userService.loginUser(user)) {
            switchToCourseSearch();
        } else {
            displayLabel("Invalid username or password.");
        }
    }

    @FXML
    public void handleRegisterButton() {
        String username = registerUsernameField.getText().trim();
        String password = registerPasswordField.getText();

//        if (username.isEmpty() || password.isEmpty()) {
//            showLabel("Username and password cannot be empty.");
//            return;
//        }

        User user = new User(username, password);
        Optional<String> registerResult = userService.registerUser(user);
        if (registerResult.isPresent()) {
            displayLabel(registerResult.get());
        } else {
            displayLabel("Registration successful! Please log in.");
            toggleForms();
        }
    }

    @FXML
    public void toggleForms() {
        if (isLoginMode) {
            loginBox.setVisible(false);
            loginBox.setManaged(false);

            registrationBox.setVisible(true);
            registrationBox.setManaged(true);

            toggleLabel.setText("Already have an account? Log In");
        } else {
            loginBox.setVisible(true);
            loginBox.setManaged(true);

            registrationBox.setVisible(false);
            registrationBox.setManaged(false);

            toggleLabel.setText("Don't have an account? Sign Up");
        }

        isLoginMode = !isLoginMode;
        label.setVisible(false);
    }

    private void displayLabel(String message) {
        label.setText(message);
        label.setVisible(true);
    }

    private void switchToCourseSearch() {
        try {
            var fxmlLoader = new FXMLLoader(getClass().getResource("course-search.fxml"));
            var newScene = new Scene(fxmlLoader.load());
            var controller = (CourseSearchController) fxmlLoader.getController();
            controller.setPrimaryStage(primaryStage);
            controller.setServices(userService, courseService, reviewService);
            primaryStage.setScene(newScene);
            primaryStage.show();
        } catch (IOException e) {
            displayLabel("Failed to load the Course Search screen.");
        }
    }
}
