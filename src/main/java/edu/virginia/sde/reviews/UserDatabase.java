package edu.virginia.sde.reviews;
import java.sql.*;
import java.util.Optional;

public class UserDatabase {
    private final DatabaseConnection DATABASE_CONNECTION;

    public UserDatabase(DatabaseConnection DATABASE_CONNECTION) {
        this.DATABASE_CONNECTION = DATABASE_CONNECTION;
    }

    public void addUser(User user) throws SQLException {
        try (Connection connection = DATABASE_CONNECTION.getConnection();
             PreparedStatement addUserStatement = connection.prepareStatement("""
                INSERT INTO Users (Username, Password)
                VALUES (?, ?);
            """)) {
            addUserStatement.setString(1, user.getUsername());
            addUserStatement.setString(2, user.getPassword());
            addUserStatement.executeUpdate();
        } catch (SQLException e) {
            DATABASE_CONNECTION.rollback();
            throw e;
        }
    }

    public boolean checkUserNameExists(String username) throws SQLException {
        try (Connection connection = DATABASE_CONNECTION.getConnection();
             PreparedStatement checkUserName = connection.prepareStatement("""
                SELECT 1 FROM Users WHERE Username = ?;
                """)) {
            checkUserName.setString(1, username);
            ResultSet resultSet = checkUserName.executeQuery();
            return resultSet.next();
        }
    }

    public Optional<User> getUserByUsername(String username) throws SQLException {
        try (Connection connection = DATABASE_CONNECTION.getConnection();
             PreparedStatement getUserStatement = connection.prepareStatement("""
                 SELECT UserID, Username, Password 
                 FROM Users WHERE Username = ?;
             """)) {
            getUserStatement.setString(1, username);
            ResultSet resultSet = getUserStatement.executeQuery();

            if (resultSet.next()) {
                int userId = resultSet.getInt("UserID");
                String fetchedUsername = resultSet.getString("Username");
                String password = resultSet.getString("Password");
                return Optional.of(new User(userId, fetchedUsername, password));
            }

            return Optional.empty();
        }
    }
}
