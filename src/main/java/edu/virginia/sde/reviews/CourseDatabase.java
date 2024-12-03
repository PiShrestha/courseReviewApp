package edu.virginia.sde.reviews;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDatabase {
    private final DatabaseConnection DATABASE_CONNECTION;

    public CourseDatabase(DatabaseConnection DATABASE_CONNECTION) {
        this.DATABASE_CONNECTION = DATABASE_CONNECTION;
    }

    public void addCourse(Course course) throws SQLException {
        try (Connection connection = DATABASE_CONNECTION.getConnection();
             PreparedStatement addCourseStatement = connection.prepareStatement("""
                INSERT INTO Courses (SubjectMnemonic, CourseNumber, Title)
                VALUES (?, ?, ?);
             """)) {
            addCourseStatement.setString(1, course.getMnemonic());
            addCourseStatement.setString(2, course.getNumber());
            addCourseStatement.setString(3, course.getTitle());
            addCourseStatement.executeUpdate();
        } catch (SQLException e) {
            DATABASE_CONNECTION.rollback();
            throw e;
        }
    }

    public Course getCourseById(int courseId) throws SQLException {
        try (Connection connection = DATABASE_CONNECTION.getConnection();
             PreparedStatement getCourseStatement = connection.prepareStatement("""
                SELECT CourseID, SubjectMnemonic, CourseNumber, Title
                FROM Courses WHERE CourseID = ?;
             """)) {
            getCourseStatement.setInt(1, courseId);
            ResultSet resultSet = getCourseStatement.executeQuery();

            if (resultSet.next()) {
                return new Course(
                        resultSet.getInt("CourseID"),
                        resultSet.getString("SubjectMnemonic"),
                        resultSet.getString("CourseNumber"),
                        resultSet.getString("Title")
                );
            }
            return null;
        }
    }

    public List<Course> searchCourses(String searchTerm) throws SQLException {
        List<Course> courses = new ArrayList<>();
        try (Connection connection = DATABASE_CONNECTION.getConnection();
             PreparedStatement searchCoursesStatement = connection.prepareStatement("""
                SELECT CourseID, SubjectMnemonic, CourseNumber, Title
                FROM Courses
                WHERE LOWER(SubjectMnemonic) LIKE LOWER(?) OR
                      CourseNumber LIKE ? OR
                      LOWER(Title) LIKE LOWER(?);
             """)) {
            String substringSearch = "%" + searchTerm + "%";
            searchCoursesStatement.setString(1, substringSearch);
            searchCoursesStatement.setString(2, substringSearch);
            searchCoursesStatement.setString(3, substringSearch);
            ResultSet resultSet = searchCoursesStatement.executeQuery();

            while (resultSet.next()) {
                courses.add(new Course(
                        resultSet.getInt("CourseID"),
                        resultSet.getString("SubjectMnemonic"),
                        resultSet.getString("CourseNumber"),
                        resultSet.getString("Title")
                ));
            }
        }
        return courses;
    }

    public List<Course> getAllCourses() throws SQLException {
        List<Course> courses = new ArrayList<>();
        try (Connection connection = DATABASE_CONNECTION.getConnection();
             PreparedStatement getAllCoursesStatement = connection.prepareStatement("""
                SELECT CourseID, SubjectMnemonic, CourseNumber, Title
                FROM Courses;
             """)) {
            ResultSet resultSet = getAllCoursesStatement.executeQuery();

            while (resultSet.next()) {
                courses.add(new Course(
                        resultSet.getInt("CourseID"),
                        resultSet.getString("SubjectMnemonic"),
                        resultSet.getString("CourseNumber"),
                        resultSet.getString("Title")
                ));
            }
        }
        return courses;
    }
}
