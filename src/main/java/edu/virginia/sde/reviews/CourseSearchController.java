package edu.virginia.sde.reviews;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class CourseSearchController {
    @FXML
    private TextField searchField;

    @FXML
    private TableView<Course> coursesTable;

    @FXML
    private TableColumn<Course, String> mnemonicColumn;

    @FXML
    private TableColumn<Course, Integer> numberColumn;

    @FXML
    private TableColumn<Course, String> titleColumn;

    @FXML
    private TableColumn<Course, String> ratingColumn;

    private CourseService courseService;
    private UserService userService;
    private ReviewService reviewService;
    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setServices(UserService userService, CourseService courseService, ReviewService reviewService) {
        this.courseService = courseService;
        this.userService = userService;
        this.reviewService = reviewService;
        loadCourses();
    }

    @FXML
    private void initialize() {
        mnemonicColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMnemonic()));
        numberColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getNumber()));
        titleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        ratingColumn.setCellValueFactory(data -> {
            Course course = data.getValue();
            double averageRating = reviewService.getAverageRatingForCourse(course.getId());
            return new SimpleStringProperty(averageRating == 0.0 ? "No Reviews" : String.format("%.2f", averageRating));
        });
    }

    private void loadCourses() {
        List<Course> courses = courseService.getAllCourses();
        coursesTable.setItems(FXCollections.observableList(courses));
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText();
        List<Course> courses = courseService.searchCourses(searchTerm);
        coursesTable.setItems(FXCollections.observableList(courses));
    }

    @FXML
    private void handleAddCourse() {
        // Create a dialog for course input
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Course");
        dialog.setHeaderText("Enter course details in the format: Mnemonic, Number, Title");

        // Show the dialog and capture user input
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            String[] parts = input.split(",", 3);
            if (parts.length != 3) {
                showError("Invalid format. Please use: Mnemonic, Number, Title");
                return;
            }

            try {
                String mnemonic = parts[0].trim();
                int number = Integer.parseInt(parts[1].trim());
                String title = parts[2].trim();

                Course newCourse = new Course(mnemonic, number, title);
                courseService.addCourse(newCourse);
                loadCourses(); // Refresh the table
            } catch (NumberFormatException e) {
                showError("Course number must be a valid 4-digit integer.");
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
            }
        });
    }

    @FXML
    private void navigateToMyReviews() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/my-reviews.fxml"));
            Scene myReviewsScene = new Scene(loader.load());

            MyReviewsController controller = loader.getController();
            controller.setServices(userService, courseService, reviewService); // Pass services
            controller.setPrimaryStage(primaryStage);

            primaryStage.setScene(myReviewsScene);
        } catch (IOException e) {
            showError("Failed to load My Reviews screen.");
        }
    }

    @FXML
    private void logout() {
        try {
            userService.logout(); // Clear the current user

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/login.fxml"));
            Scene loginScene = new Scene(loader.load());

            LoginController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            controller.setServices(userService, courseService, reviewService);

            primaryStage.setScene(loginScene);
        } catch (IOException e) {
            showError("Failed to load Login screen.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}


