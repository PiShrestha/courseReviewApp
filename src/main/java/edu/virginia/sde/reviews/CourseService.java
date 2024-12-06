package edu.virginia.sde.reviews;

import java.util.*;
import java.sql.*;

public class CourseService {
    private final CourseDatabase courseDatabase;

    public CourseService(CourseDatabase courseDatabase) {
        this.courseDatabase = courseDatabase;
    }

    public List<Course> searchCourses(String searchTerm){
        try {
            return courseDatabase.searchCourses(searchTerm);
        } catch (SQLException e) {
            throw new RuntimeException("Database error occurred", e);
        }
    }

    public void addCourse(Course course) {

        if (course.getMnemonic() == null || course.getMnemonic().length() < 2 || course.getMnemonic().length() > 4 || !course.getMnemonic().matches("[a-zA-Z]+")) {
            throw new IllegalArgumentException("Invalid subject: must be 2-4 letters.");
        }
        if (course.getNumber() == null || !course.getNumber().matches("\\d{4}")) {
            throw new IllegalArgumentException("Invalid course number: must be exactly 4 digits.");
        }
        if (course.getTitle() == null || course.getTitle().length() < 1 || course.getTitle() .length() > 50) {
            throw new IllegalArgumentException("Invalid title: must be between 1 and 50 characters.");
        }

        try {
            Course newCourse = new Course(course.getMnemonic().toUpperCase(),course.getNumber(), course.getTitle());
            courseDatabase.addCourse(newCourse);
        } catch (SQLException e) {
            throw new RuntimeException("Database error occurred", e);
        }
    }

    public Optional<Course> getCourseById(int courseId) {
        try {
            return Optional.ofNullable(courseDatabase.getCourseById(courseId));
        } catch (SQLException e) {
            throw new RuntimeException("Database error occurred", e);
        }
    }

    public List<Course> getAllCourses(){
        try {
            return courseDatabase.getAllCourses();
        } catch (SQLException e) {
            throw new RuntimeException("Database error occurred", e);
        }
    }

    public boolean courseExists(Course course) {
        try {
            return courseDatabase.courseExists(course);
        } catch (SQLException e) {
            throw new RuntimeException("Database error occurred", e);
        }
    }


}
