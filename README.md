[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/QrU2hpdx)
# Homework 6 - Responding to Change

## Authors
1) Pratik Shrestha, jpj8rf, [PiShrestha]
2) Kyle Luong, auj7tx, [kyle-luong]
3) Abhinav Potluri, nsv9qf, [abhinvpotluri01]

## To Run

## Running the Application

#### If macuser, then the permissions gradlew file needs to be executed:
`chmod +x gradlew`

#### Use the following command to compile and build the application:
`./gradlew build`

#### Start the application with:
`./gradlew run`

Ensure the login screen is displayed upon running the application.

## Contributions

List the primary contributions of each author. It is recommended to update this with your contributions after each coding session.:

### Pratik Shrestha

* Created Review, User, and Course models.
* Built UserDatabase and CourseDatabase to manage database operations like adding, updating and retrieving data. 
* Developed UserService and CourseService to handle app logic and connect controllers to the database.
* Made LoginController for user login and registration, with a matching login screen.
* Built CourseReviewsController for adding, updating, and viewing reviews, with a user-friendly interface.

### Kyle Luong

* Designed initial structure for models, database, services, and controllers with fxml
* Implemented DatabaseConnection for a single main database and ReviewsDatabase to describe the Reviews table
* Implemented CourseService for services interacting with course data
* Changed CourseReviewApplication for project specifications
* Fixes in UserService, LoginController, CourseDatabase, and fxml for titles and login background
* Did system testing and minor specifications like duplicate courses, enter keypresses, ect.

### Abhinav Potluri

* Implemented MyReviewsController and MyReviews.fxml to make the functionality for a user to see their reviews
* Implemented CourseSearchController and CourseSearch.fxml to allow a user to search for a course
* Added Course Name and Average Rating fields to CourseReviewsController and CourseReviews.fxml
* Implemented method for deleting reviews in CourseReviewsController, and added a button for deletion in CourseReviews.fxml
* Did system testing

## Issues

N/A
