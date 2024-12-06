package edu.virginia.sde.reviews;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @FXML
    private TableColumn<Review, Void> deleteColumn;

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
        // Referenced for DateTimeFormatter.ofPattern usage:
        // https://stackoverflow.com/questions/35156809/parsing-a-date-using-datetimeformatter-ofpattern
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
        commentColumn.setCellFactory(param -> new TableCell<>() {

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Text text = new Text(item);
                    text.wrappingWidthProperty().bind(commentColumn.widthProperty());
                    setGraphic(text);
                }
            }
        });

        deleteColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setStyle("-fx-background-color: #dd473c; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                deleteButton.setOnAction(event -> {
                    Review review = getTableView().getItems().get(getIndex());
                    handleDeleteReview(review);
                });
                setGraphic(deleteButton);
            }
        });
        deleteColumn.setStyle("-fx-alignment: CENTER;");

        reviewsTable.setPlaceholder(new Label("You have no Reviews available."));
        reviewsTable.setFocusTraversable(false);
        setupRowFactory();
    }

    private void setupRowFactory() {
        reviewsTable.setRowFactory(tv -> {
            TableRow<Review> row = new TableRow<>();
            row.setOnMouseClicked(event -> handleRowClick(event, row));
            return row;
        });
    }

    private void handleRowClick(MouseEvent event, TableRow<Review> row) {
        if (event.getClickCount() == 2 && (!row.isEmpty())) {
            Review selectedReview = row.getItem();
            int courseId = selectedReview.getCourseId();
            Course course = courseService.getCourseById(courseId).get();
            navigateToCourseReviews(course);
        }
    }

    private void loadReviews() {
        User currentUser = userService.getCurrentUser();
        List<Review> reviews = reviewService.getReviewsByUser(currentUser.getId());
        reviewsTable.setItems(FXCollections.observableList(reviews));
    }

    private void handleDeleteReview(Review review) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Are you sure you want to delete this review?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Optional<String> deleteResult = reviewService.deleteReview(review.getId());
            if (deleteResult.isPresent()) {
                showError("Error: " + deleteResult.get());
            } else {
                // Remove review from TableView
                reviewsTable.getItems().remove(review);
                showInfo("Review deleted successfully.");
            }
        }
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

    @FXML
    private void navigateToCourseReviews(Course selectedCourse) {
        if (selectedCourse == null) {
            showError("Please select a course to view reviews.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/CourseReviews.fxml"));
            Scene courseReviewsScene = new Scene(loader.load());

            CourseReviewsController controller = loader.getController();
            controller.setCourseId(selectedCourse.getId());
            controller.setServices(userService, courseService, reviewService);
            controller.setPrimaryStage(primaryStage);

            primaryStage.setScene(courseReviewsScene);
        } catch (IOException e) {
            showError("Failed to load the Course Reviews screen.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getCourseNameById(int courseId) {
        Optional<Course> course = courseService.getCourseById(courseId);
        if (course.isPresent()) {
            Course c = course.get();
            String mnemonic = c.getMnemonic();
            String number = c.getNumber();
            String title = c.getTitle();
            return mnemonic + " " + number + ": " + title;
        } else {
            return "Unknown Course";
        }
    }

}

