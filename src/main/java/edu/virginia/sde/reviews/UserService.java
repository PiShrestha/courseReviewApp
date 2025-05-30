package edu.virginia.sde.reviews;
import java.sql.*;
import java.util.Optional;

public class UserService {
    private final UserDatabase userDatabase;
    private User currentUser;

    public UserService(UserDatabase userDatabase) {
        this.userDatabase = userDatabase;
    }

    public Optional<String> registerUser(User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Optional.of("Username cannot be empty.");
        }

        if (user.getPassword() == null || user.getPassword().length() < 8) {
            return Optional.of("Password must be at least 8 characters.");
        }

        try {
            if (userDatabase.checkUserNameExists(user.getUsername())) {
                return Optional.of("Username already exists. Please try a new username.");
            }
            userDatabase.addUser(user);
            return Optional.empty();
        } catch (SQLException e) {
            return Optional.of("Error registering user. Please try again later.");
        }
    }

    public boolean loginUser(User user) {
        try {
            Optional<User> userFromDatabase = userDatabase.getUserByUsername(user.getUsername());

            if (userFromDatabase.isPresent()) {
                User fetchedUser = userFromDatabase.get();
                if (user.getPassword().equals(fetchedUser.getPassword())) {
                    currentUser = fetchedUser;
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Database error occurred", e);
        }
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
