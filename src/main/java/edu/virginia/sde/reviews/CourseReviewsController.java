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
import java.util.stream.Collectors;

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

    @FXML
    private TableColumn<Review, Void> actionColumn;

    @FXML
    private TextField averageRatingField;

    @FXML
    private TextField courseNameField;

    @FXML
    private Label courseAverageLabel;
    @FXML
    private Label courseNameLabel;

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
        addActionButtonToTable();
    }

    @FXML
    private void loadReviews() {
        try {
            List<Review> reviews = reviewService.getReviewsForCourse(courseId);
            reviewsTable.setItems(FXCollections.observableList(reviews));

            Optional<Course> course = courseService.getCourseById(courseId);
            if (course.isPresent()) {
                // courseNameField.setText(course.get().getTitle());
                double averageRating = reviewService.getAverageRatingForCourse(courseId);
                // averageRatingField.setText(String.format("%.2f", averageRating));
                courseNameLabel.setText("Course name: " + course.get().getTitle());
                courseAverageLabel.setText(String.format("Course Average: %.2f", averageRating));
            }
            else {
                courseNameField.setText("Course not found");
                averageRatingField.setText("N/A");
            }
        } catch (Exception e) {
            showError("Failed to load reviews. Please try again.");
        }
    }

    private void addActionButtonToTable() {
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Edit");

            {
                editButton.setOnAction(event -> {
                    Review review = getTableView().getItems().get(getIndex());
                    handleEditReview(review);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                Review review = getTableView().getItems().get(getIndex());

                if (review.getUserId() == userService.getCurrentUser().getId()) {
                    setGraphic(editButton);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void handleEditReview(Review review) {
        if (review.getUserId() == userService.getCurrentUser().getId()) {
            ratingField.setText(String.valueOf(review.getRating()));
            commentTextArea.setText(review.getComment());
        } else {
            showError("You can only edit your own review.");
        }
    }

    @FXML
    private void handleAddReview() {
        String comment = commentTextArea.getText().trim();
        String ratingInput = ratingField.getText().trim();

        if (ratingInput.isEmpty()) {
            showError("A rating is required.");
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

    @FXML
    private void handleUpdateReview() {
        String comment = commentTextArea.getText().trim();
        String ratingInput = ratingField.getText().trim();

        if (ratingInput.isEmpty()) {
            showError("A rating is required.");
            return;
        }

        try {
            int rating = Integer.parseInt(ratingInput);

            Optional<Review> userReview = reviewService.getReviewsForCourse(courseId).stream()
                    .filter(review -> review.getUserId() == userService.getCurrentUser().getId())
                    .findFirst();

            if (userReview.isPresent()) {
                Review updatedReview = userReview.get();
                updatedReview.setRating(rating);
                updatedReview.setComment(comment);
                updatedReview.setTimestamp(new Timestamp(System.currentTimeMillis()));

                Optional<String> result = reviewService.updateReview(updatedReview);

                if (result.isPresent()) {
                    showError(result.get());
                } else {
                    showSuccess("Review updated successfully!");
                    loadReviews();
                }
            } else {
                showError("No review found to update.");
            }
        } catch (NumberFormatException e) {
            showError("Rating must be a valid number.");
        } catch (Exception e) {
            showError("An error occurred while updating the review.");
        }
    }

    @FXML
    private void handleDeleteReview() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Review");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to delete this review?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete the review
                Optional<Review> userReview = reviewService.getReviewsForCourse(courseId).stream()
                        .filter(review -> review.getUserId() == userService.getCurrentUser().getId())
                        .findFirst();
                Optional<String> deleteResult = reviewService.deleteReview(userReview.get().getId());
                if (deleteResult.isPresent()) {
                    showError(deleteResult.get());
                } else {
                    showSuccess("Review deleted successfully!");
                    loadReviews(); // Reload the reviews table
                }
            } catch (Exception e) {
                showError("An error occurred while deleting the review.");
            }
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
