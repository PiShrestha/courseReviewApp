package edu.virginia.sde.reviews;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class CourseReviewsController {

    @FXML
    private Label messageLabel;

    @FXML
    private TableView<Review> reviewsTable;

    @FXML
    private TableColumn<Review, Integer> ratingColumn;

    @FXML
    private TableColumn<Review, String> commentColumn;

    @FXML
    private TableColumn<Review, Timestamp> timestampColumn;

    @FXML
    private TextArea commentTextArea;

    @FXML
    private TextField ratingField;

    private UserService userService;
    private CourseService courseService;
    private ReviewService reviewService;
    private Stage primaryStage;
    private int courseId;

    public void setServices(UserService userService, CourseService courseService, ReviewService reviewService) {
        this.userService = userService;
        this.courseService = courseService;
        this.reviewService = reviewService;
        loadReviews();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    @FXML
    private void initialize() {
        ratingColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getRating()));
        commentColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getComment()));
        timestampColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getTimestamp()));

        messageLabel.setText("");
    }

    @FXML
    private void loadReviews() {
        try {
            List<Review> reviews = reviewService.getReviewsForCourse(courseId);
            reviewsTable.setItems(FXCollections.observableList(reviews));
        } catch (Exception e) {
            showError("Failed to load reviews. Please try again.");
        }
    }

    @FXML
    private void handleAddReview() {
        String comment = commentTextArea.getText().trim();
        String ratingInput = ratingField.getText().trim();

        if (comment.isEmpty() || ratingInput.isEmpty()) {
            showError("Both rating and comment are required.");
            return;
        }

        try {
            int rating = Integer.parseInt(ratingInput);
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            Review newReview = new Review(courseId, userService.getCurrentUser().getId(), rating, comment, currentTimestamp);

            Optional<String> result = reviewService.createReview(newReview);

            if (result.isPresent()) {
                showError(result.get());
            } else {
                showSuccess("Review added successfully!");
                commentTextArea.clear();
                ratingField.clear();
                loadReviews();
            }
        } catch (NumberFormatException e) {
            showError("Rating must be a valid number.");
        } catch (Exception e) {
            showError("An error occurred while adding the review.");
        }
    }

    private void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setText(message);
    }

    private void showSuccess(String message) {
        messageLabel.setStyle("-fx-text-fill: green;");
        messageLabel.setText(message);
    }

    @FXML
    private void navigateToCourseSearch() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/CourseSearch.fxml"));
            Scene courseSearchScene = new Scene(loader.load());

            CourseSearchController controller = loader.getController();
            controller.setServices(userService, courseService, reviewService);
            controller.setPrimaryStage(primaryStage);

            primaryStage.setScene(courseSearchScene);
        } catch (IOException e) {
            showError("Failed to load the Course Search screen.");
        }
    }
}
