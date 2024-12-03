package edu.virginia.sde.reviews;
import java.sql.*;

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
}
