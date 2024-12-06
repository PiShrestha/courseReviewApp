package edu.virginia.sde.reviews;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
    private Label loginErrorLabel;

    @FXML
    private Label registerErrorLabel;

    @FXML
    private Label loginLabel;

    @FXML
    private Label signUpLabel;

    @FXML
    private Label registerSuccessLabel;

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
        if (loginErrorLabel != null) {
            loginErrorLabel.setVisible(false);
            loginErrorLabel.setManaged(false);
        }
        if (registerErrorLabel != null) {
            registerErrorLabel.setVisible(false);
            registerErrorLabel.setManaged(false);
        }
        if (registerSuccessLabel != null) {
            registerSuccessLabel.setVisible(false);
            registerSuccessLabel.setManaged(false);
        }

        usernameField.setOnKeyPressed(event -> handleEnterOnInput(event));
        passwordField.setOnKeyPressed(event -> handleEnterOnInput(event));
        registerUsernameField.setOnKeyPressed(event -> handleEnterOnInput(event));
        registerPasswordField.setOnKeyPressed(event -> handleEnterOnInput(event));

        usernameField.setFocusTraversable(false);
        registerUsernameField.setFocusTraversable(false);
        passwordField.setFocusTraversable(false);
        registerPasswordField.setFocusTraversable(false);
    }

    private void handleEnterOnInput(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            if (isLoginMode) {
                handleLoginButton();
            } else {
                handleRegisterButton();
            }
        }
    }

    @FXML
    public void handleLoginButton() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            displayError("Username and password cannot be empty.");
            return;
        }

        User user = new User(username, password);
        if (userService.loginUser(user)) {
            switchToCourseSearch();
        } else {
            displayError("Invalid username or password.");
        }
    }

    @FXML
    public void handleCloseButton() {
        Platform.exit();
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
            displayError(registerResult.get());
        } else {
            displaySuccess("Registration successful! Please log in.");
            toggle();
        }
    }

    @FXML
    public void toggle() {
        if (isLoginMode) {
            loginBox.setVisible(false);
            loginBox.setManaged(false);

            // clear() so that username and password fields are cleared when switching between the login box and registration box
            usernameField.clear();
            passwordField.clear();
            registrationBox.setVisible(true);
            registrationBox.setManaged(true);

            toggleLabel.setText("Already have an account? Log In");
        } else {
            loginBox.setVisible(true);
            loginBox.setManaged(true);

            registerUsernameField.clear();
            registerPasswordField.clear();
            registrationBox.setVisible(false);
            registrationBox.setManaged(false);

            toggleLabel.setText("Don't have an account? Sign Up");
        }

        isLoginMode = !isLoginMode;

        loginErrorLabel.setVisible(false);
        loginErrorLabel.setManaged(false);
        registerErrorLabel.setVisible(false);
        registerErrorLabel.setManaged(false);
        registerSuccessLabel.setVisible(false);
        registerSuccessLabel.setManaged(false);
    }

    private void displayError(String message) {
        if (isLoginMode) {
            loginErrorLabel.setText(message);
            loginErrorLabel.setVisible(true);
            loginErrorLabel.setManaged(true);
        } else {
            registerErrorLabel.setText(message);
            registerErrorLabel.setVisible(true);
            registerErrorLabel.setManaged(true);
            registerSuccessLabel.setVisible(false);
            registerSuccessLabel.setManaged(false);
        }
    }

    private void displaySuccess(String message) {
        registerSuccessLabel.setText(message);
        registerSuccessLabel.setVisible(true);
        registerSuccessLabel.setManaged(true);
        registerErrorLabel.setVisible(false);
        registerErrorLabel.setManaged(false);
    }

    private void switchToCourseSearch() {
        try {
            var fxmlLoader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/CourseSearch.fxml"));
            var newScene = new Scene(fxmlLoader.load());
            var controller = (CourseSearchController) fxmlLoader.getController();
            controller.setPrimaryStage(primaryStage);
            controller.setServices(userService, courseService, reviewService);
            primaryStage.setScene(newScene);
            primaryStage.show();
        } catch (IOException e) {
            displayError("Failed to load the Course Search screen.");
        }
    }
}
