package edu.virginia.sde.reviews;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class CourseReviewsController {

    @FXML
    private Label messageLabel;

    @FXML
    private TableView<Review> reviewsTable;

    @FXML
    private TableColumn<Review, Void> currentUser;

    @FXML
    private TableColumn<Review, Integer> ratingColumn;

    @FXML
    private TableColumn<Review, String> commentColumn;

    @FXML
    private TableColumn<Review, String> timestampColumn;

    @FXML
    private VBox reviewBox;

    @FXML
    private TextArea commentTextArea;

    @FXML
    private TextField ratingField;

    @FXML
    private Button addReviewButton;

    @FXML
    private Button editReviewButton;

    @FXML
    private Button deleteReviewButton;

    @FXML
    private Button submitReviewButton;

    @FXML
    private Button cancelReviewButton;

    @FXML
    private Label courseAverageLabel;
    @FXML
    private Label courseNameLabel;

    private UserService userService;
    private CourseService courseService;
    private ReviewService reviewService;
    private Stage primaryStage;
    private int courseId;

    // This is only true when user is updating their code
    private boolean isUpdating = false;

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm a");
        timestampColumn.setCellValueFactory(data -> {
            Timestamp timestamp = data.getValue().getTimestamp();
            if (timestamp != null) {
                LocalDateTime dateTime = timestamp.toLocalDateTime();
                String formattedTimestamp = dateTime.format(formatter);
                return new SimpleObjectProperty<>(formattedTimestamp);
            } else {
                return new SimpleObjectProperty<>("");
            }
        });

        // New column so the user can know which one is their comment
        currentUser.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Review review = getTableView().getItems().get(getIndex());
                    if (review.getUserId() == userService.getCurrentUser().getId()) {
                        setGraphic(new Label("*"));
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        reviewsTable.setFocusTraversable(false);
        commentTextArea.setFocusTraversable(false);
        commentTextArea.setWrapText(true);
        reviewsTable.setPlaceholder(new Label("No reviews available for this course."));

        // Initially, the reviewBox is not visible
        reviewBox.setVisible(false);
        reviewBox.setManaged(false);

        // The following code dynamically toggles the reviewBox's visibility based on the presence of the userReview
        editReviewButton.setVisible(false);
        editReviewButton.setManaged(false);
        deleteReviewButton.setVisible(false);
        deleteReviewButton.setManaged(false);
    }

    @FXML
    private void loadReviews() {
        try {
            List<Review> reviews = reviewService.getReviewsForCourse(courseId);
            reviewsTable.setItems(FXCollections.observableList(reviews));

            Optional<Course> course = courseService.getCourseById(courseId);
            if (course.isPresent()) {
                double averageRating = reviewService.getAverageRatingForCourse(courseId);
                courseNameLabel.setText(course.get().getMnemonic() + " " + course.get().getNumber() + ": " + course.get().getTitle());
                courseAverageLabel.setText(String.format("Course Average: %.2f", averageRating));
            }

            boolean hasUserReview = reviews.stream()
                    .anyMatch(review -> review.getUserId() == userService.getCurrentUser().getId());

            // Add Review Button is always visible
            addReviewButton.setVisible(true);
            addReviewButton.setManaged(true);

            // Edit and Delete Review Button is only visible when User has a review
            editReviewButton.setVisible(hasUserReview);
            editReviewButton.setManaged(hasUserReview);
            deleteReviewButton.setVisible(hasUserReview);
            deleteReviewButton.setManaged(hasUserReview);
        } catch (Exception e) {
            showError("Failed to load reviews. Please try again.");
        }
    }

    @FXML
    private void handleAddReview() {
        Optional<Review> userReview = reviewService.getReviewsForCourse(courseId).stream()
                .filter(review -> review.getUserId() == userService.getCurrentUser().getId())
                .findFirst();

        if (userReview.isPresent()) {
            showError("You already have a review for this course. You can only edit your review instead.");
        } else {
            isUpdating = false;
            toggleReviewBox(true, "Submit Review");
            commentTextArea.clear();
            ratingField.clear();
        }
    }

    @FXML
    private void handleEditReview() {
        Optional<Review> userReview = reviewService.getReviewsForCourse(courseId).stream()
                .filter(review -> review.getUserId() == userService.getCurrentUser().getId())
                .findFirst();

        if (userReview.isPresent()) {
            isUpdating = true;
            Review review = userReview.get();
            ratingField.setText(String.valueOf(review.getRating()));
            commentTextArea.setText(review.getComment());
            toggleReviewBox(true, "Update Review");
        } else {
            showError("No review found to edit.");
        }
    }

    @FXML
    private void handleDeleteReview() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Review");
        confirmAlert.setHeaderText("Are you sure you want to delete your review?");
        confirmAlert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Optional<Review> userReview = reviewService.getReviewsForCourse(courseId).stream()
                        .filter(review -> review.getUserId() == userService.getCurrentUser().getId())
                        .findFirst();

                if (userReview.isEmpty()) {
                    showError("No review found to delete.");
                    return;
                }

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

    //Combined update and add Review into one function
    @FXML
    private void handleSubmitReview() {
        String comment = commentTextArea.getText().trim();
        String ratingInput = ratingField.getText().trim();

        if (ratingInput.isEmpty()) {
            showError("A rating is required.");
            return;
        }

        try {
            int rating = Integer.parseInt(ratingInput);
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

            if (isUpdating) {
                Optional<Review> userReview = reviewService.getReviewsForCourse(courseId).stream()
                        .filter(review -> review.getUserId() == userService.getCurrentUser().getId())
                        .findFirst();

                if (userReview.isPresent()) {
                    Review updatedReview = userReview.get();
                    updatedReview.setRating(rating);
                    updatedReview.setComment(comment);
                    updatedReview.setTimestamp(currentTimestamp);
                    reviewService.updateReview(updatedReview);
                    showSuccess("Review updated successfully!");
                } else {
                    showError("No review found to update.");
                }
            } else {
                Review newReview = new Review(courseId, userService.getCurrentUser().getId(), rating, comment, currentTimestamp);
                reviewService.createReview(newReview);
                showSuccess("Review added successfully!");
            }

            // Once the user successfully adds the review, disable the Review Box
            toggleReviewBox(false, "");
            loadReviews();
        } catch (NumberFormatException e) {
            showError("Rating must be a valid integer number.");
        } catch (Exception e) {
            showError("An error occurred while updating the review.");
        }
    }

    @FXML
    private void handleCancelReview() {
        toggleReviewBox(false, "");
        commentTextArea.clear();
        ratingField.clear();
        isUpdating = false;
    }

    // pass "" in buttonText clears the textFields
    private void toggleReviewBox(boolean visible, String buttonText) {
        reviewBox.setVisible(visible);
        reviewBox.setManaged(visible);
        submitReviewButton.setText(buttonText);
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
