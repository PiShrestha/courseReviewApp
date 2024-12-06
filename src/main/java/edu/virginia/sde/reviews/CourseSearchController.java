package edu.virginia.sde.reviews;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
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
    private TableColumn<Course, String> numberColumn;

    @FXML
    private TableColumn<Course, String> titleColumn;

    @FXML
    private TableColumn<Course, String> ratingColumn;

    @FXML
    private VBox addCourseForm;

    @FXML
    private TextField mnemonicField;

    @FXML
    private TextField numberField;

    @FXML
    private TextField titleField;

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

        setupRowFactory();
        searchField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) handleSearch();
        });

        coursesTable.setFocusTraversable(false);
        coursesTable.setPlaceholder(new Label("No courses available."));
    }

    private void setupRowFactory() {
        coursesTable.setRowFactory(tv -> {
            TableRow<Course> row = createRowWithClickListener();
            return row;
        });
    }

    private TableRow<Course> createRowWithClickListener() {
        TableRow<Course> row = new TableRow<>();
        row.setOnMouseClicked(event -> handleRowClick(event, row));
        return row;
    }

    private void handleRowClick(MouseEvent event, TableRow<Course> row) {
        if (event.getClickCount() == 2 && (!row.isEmpty())) {
            Course selectedCourse = row.getItem();
            navigateToCourseReviews(selectedCourse);
        }
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
    private void showAddCourseForm() {
        addCourseForm.setVisible(true);
        addCourseForm.setManaged(true);
    }

    @FXML
    private void hideAddCourseForm() {
        addCourseForm.setVisible(false);
        addCourseForm.setManaged(false);
        clearAddCourseForm();
    }

    @FXML
    private void handleSubmitCourse() {
        try {
            String mnemonic = mnemonicField.getText().trim().toUpperCase();
            String numberText = numberField.getText().trim();
            String title = titleField.getText().trim();

            Course newCourse = new Course(mnemonic, numberText, title);
            if (courseService.courseExists(newCourse)) {
                throw new IllegalArgumentException("Course already exists.");
            }
            courseService.addCourse(newCourse);
            loadCourses(); // Refresh the table
            hideAddCourseForm();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private void clearAddCourseForm() {
        mnemonicField.clear();
        numberField.clear();
        titleField.clear();
    }

    @FXML
    private void navigateToMyReviews() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/MyReviews.fxml"));
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

}


