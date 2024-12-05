package edu.virginia.sde.reviews;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class MyReviewsController {
    @FXML
    private TableView<Review> reviewsTable;

    @FXML
    private TableColumn<Review, String> courseColumn;

    @FXML
    private TableColumn<Review, Integer> ratingColumn;

    @FXML
    private TableColumn<Review, String> commentColumn;

    @FXML
    private TableColumn<Review, String> timestampColumn;

    private CourseService courseService;
    private ReviewService reviewService;
    private UserService userService;
    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setServices(UserService userService, CourseService courseService, ReviewService reviewService) {
        this.reviewService = reviewService;
        this.userService = userService;
        this.courseService = courseService;
        loadReviews();
    }

    @FXML
    private void initialize() {
        courseColumn.setCellValueFactory(data -> {
            int courseId = data.getValue().getCourseId();
            String courseName = getCourseNameById(courseId); // Fetch course name from the database
            return new ReadOnlyStringWrapper(courseName);
        });
        ratingColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getRating()));
        commentColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getComment()));
        timestampColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTimestamp().toString()));
    }


    private void loadReviews() {
        User currentUser = userService.getCurrentUser();
        List<Review> reviews = reviewService.getReviewsByUser(currentUser.getId());
        reviewsTable.setItems(FXCollections.observableList(reviews));
    }

    @FXML
    private void navigateToCourseSearch() {
        try {
            // Load the Course Search FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/CourseSearch.fxml"));
            Scene courseSearchScene = new Scene(loader.load());

            // Get the controller for the CourseSearch screen
            CourseSearchController controller = loader.getController();

            // Pass the necessary services to the controller
            controller.setServices(userService, courseService, reviewService);
            controller.setPrimaryStage(primaryStage);

            // Set the new scene on the primary stage
            primaryStage.setScene(courseSearchScene);
        } catch (IOException e) {
            showError("Failed to load the Course Search screen.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getCourseNameById(int courseId) {
        Optional<Course> course = courseService.getCourseById(courseId);
        return course.map(Course::getTitle).orElse("Unknown Course");
    }

}

