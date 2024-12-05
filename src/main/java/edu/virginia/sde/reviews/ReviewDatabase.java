package edu.virginia.sde.reviews;

import java.sql.*;
import java.util.*;

public class ReviewDatabase {

    private final DatabaseConnection databaseConnection;

    public ReviewDatabase(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public void addReview(Review review) throws SQLException {
        Connection connection = databaseConnection.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement("""
                INSERT INTO Reviews (UserID, CourseID, Rating, Comment, Timestamp)
                    VALUES(?, ?, ?, ?, ?);
                """)) {
            stmt.setInt(1, review.getUserId());
            stmt.setInt(2, review.getCourseId());
            stmt.setInt(3, review.getRating());
            stmt.setString(4, review.getComment());
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
            databaseConnection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public void updateReview(Review review) throws SQLException {
        Connection connection = databaseConnection.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement("""
                UPDATE Reviews
                SET Rating = ?, Comment = ?, Timestamp = ?
                WHERE ReviewID = ?;
                """)) {
            stmt.setInt(1, review.getRating());
            stmt.setString(2, review.getComment());
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(4, review.getId());
            stmt.executeUpdate();
            databaseConnection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public void deleteReview(int reviewId) throws SQLException {
        Connection connection = databaseConnection.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement("""
                DELETE FROM Reviews WHERE ReviewID = ?;
                """)) {
            stmt.setInt(1, reviewId);
            stmt.executeUpdate();
            databaseConnection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public List<Review> getReviewsByCourseId(int courseId) throws SQLException {
        Connection connection = databaseConnection.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement("""
                SELECT * FROM Reviews WHERE CourseID = ?;
                """)) {
            stmt.setInt(1, courseId);
            ResultSet resultSet = stmt.executeQuery();

            List<Review> reviews = new ArrayList<>();
            while (resultSet.next()) {
                int id = resultSet.getInt("ReviewID");
                int userId = resultSet.getInt("UserID");
                int rating = resultSet.getInt("Rating");
                String comment = resultSet.getString("Comment");
                Timestamp timestamp = resultSet.getTimestamp("Timestamp");
                reviews.add(new Review(id, courseId, userId, rating, comment, timestamp));
            }
            return reviews;
        }
    }

    public Optional<Review> getReviewById(int reviewId) throws SQLException {
        Connection connection = databaseConnection.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement("""
                SELECT * FROM Reviews WHERE ReviewID = ?;
                """)) {
            stmt.setInt(1, reviewId);
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                int id = resultSet.getInt("ReviewID");
                int courseId = resultSet.getInt("CourseID");
                int userId = resultSet.getInt("UserID");
                int rating = resultSet.getInt("Rating");
                String comment = resultSet.getString("Comment");
                Timestamp timestamp = resultSet.getTimestamp("Timestamp");
                return Optional.of(new Review(id, courseId, userId, rating, comment, timestamp));
            } else {
                return Optional.empty();
            }
        }
    }

    public List<Review> getReviewsByUserId(int userId) throws SQLException {
        Connection connection = databaseConnection.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement("""
                SELECT * FROM Reviews WHERE UserID = ?;
                """)) {
            stmt.setInt(1, userId);
            ResultSet resultSet = stmt.executeQuery();

            List<Review> reviews = new ArrayList<>();
            while (resultSet.next()) {
                int id = resultSet.getInt("ReviewID");
                int courseId = resultSet.getInt("CourseID");
                int rating = resultSet.getInt("Rating");
                String comment = resultSet.getString("Comment");
                Timestamp timestamp = resultSet.getTimestamp("Timestamp");
                reviews.add(new Review(id, courseId, userId, rating, comment, timestamp));
            }
            return reviews;
        }
    }
}
