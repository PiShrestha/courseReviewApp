package edu.virginia.sde.reviews;

import java.util.*;
import java.sql.*;

public class CourseService {
    private final CourseDatabase courseDatabase;

    public CourseService(CourseDatabase courseDatabase) {
        this.courseDatabase = courseDatabase;
    }

    public List<Course> getAllCourses(){
        try {
            return courseDatabase.getAllCourses();
        } catch (SQLException e) {
            throw new RuntimeException("Database error occurred", e);
        }
    }

    public List<Course> searchCourses(String searchTerm){
        try {
            return courseDatabase.searchCourses(searchTerm);
        } catch (SQLException e) {
            throw new RuntimeException("Database error occurred", e);
        }
    }

    public void addCourse(String subject, String number, String title) {

        if (subject == null || subject.length() < 2 || subject.length() > 4 || !subject.matches("[a-zA-Z]+")) {
            throw new IllegalArgumentException("Invalid subject: must be 2-4 letters.");
        }
        if (number == null || !number.matches("\\d{4}")) {
            throw new IllegalArgumentException("Invalid course number: must be a 4-digit number.");
        }
        if (title == null || title.length() < 1 || title.length() > 50) {
            throw new IllegalArgumentException("Invalid title: must be between 1 and 50 characters.");
        }

        try {
            Course newCourse = new Course(subject.toUpperCase(), Integer.parseInt(number), title);
            courseDatabase.addCourse(newCourse);
        } catch (SQLException e) {
            throw new RuntimeException("Database error occurred", e);
        }
    }


}
