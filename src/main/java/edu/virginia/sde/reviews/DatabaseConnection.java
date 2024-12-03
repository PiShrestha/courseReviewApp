package edu.virginia.sde.reviews;

import java.sql.*;

public class DatabaseConnection {
    private final String sqliteFilename;
    private Connection connection;

    public DatabaseConnection(String sqliteFilename) {
        this.sqliteFilename = sqliteFilename;
    }

    /**
     * Connect to a SQLite Database. This turns out Foreign Key enforcement, and disables auto-commits
     *
     * @throws SQLException
     */
    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            throw new IllegalStateException("The connection is already opened");
        }
        connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteFilename);
        //the next line enables foreign key enforcement - do not delete/comment out
        connection.createStatement().execute("PRAGMA foreign_keys = ON");
        //the next line disables auto-commit - do not delete/comment out
        connection.setAutoCommit(false);
    }

    /**
     * Commit all changes since the connection was opened OR since the last commit/rollback
     */
    public void commit() throws SQLException {
        connection.commit();
    }

    /**
     * Rollback to the last commit, or when the connection was opened
     */
    public void rollback() throws SQLException {
        connection.rollback();
    }

    /**
     * Ends the connection to the database
     */
    public void disconnect() throws SQLException {
        connection.close();
    }

    /**
     * Creates the database tables if they do not already exist.
     *
     * @throws SQLException
     */
    public void createTables() throws SQLException {
        if (connection.isClosed()) {
            throw new IllegalStateException("Connection is already closed");
        }
        try {
            createUsersTable();
            createCoursesTable();
            createReviewsTable();
        }  catch (SQLException e) {
//            rollback();
            throw e;
        }
    }

    public void createUsersTable() throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("""
            CREATE TABLE IF NOT EXISTS Users (
                UserID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                Username TEXT NOT NULL UNIQUE,
                Password TEXT NOT NULL
            ) STRICT;
            """)) {
            stmt.executeUpdate();
        }
    }

    public void createCoursesTable() throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("""
            CREATE TABLE IF NOT EXISTS Courses (
                CourseID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                SubjectMnemonic TEXT NOT NULL,
                CourseNumber INTEGER NOT NULL,
                Title TEXT NOT NULL
                CONSTRAINT UniqueCourse UNIQUE (SubjectMnemonic, CourseNumber, Title)
            ) STRICT;
            """)) {
            stmt.executeUpdate();
        }
    }

    private void createReviewsTable() throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS Reviews (
                    ReviewID INTEGER PRIMARY KEY AUTOINCREMENT,
                    UserID INTEGER NOT NULL,
                    CourseID INTEGER NOT NULL,
                    Rating INTEGER NOT NULL,
                    Comment TEXT,
                    Timestamp TEXT NOT NULL,
                    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE,
                    FOREIGN KEY (CourseID) REFERENCES Courses(CourseID) ON DELETE CASCADE,
                    CONSTRAINT UniqueReview UNIQUE (UserID, CourseID)
                );
                """)) {
            stmt.executeUpdate();
        }
    }

    /**
     * Removes all data from the tables, leaving the tables empty (but still existing!).
     */
    public void clearTables() throws SQLException {
        try (Statement preparedStatement = connection.createStatement()) {
            preparedStatement.execute("DELETE FROM Users;");
            preparedStatement.execute("DELETE FROM Courses;");
            preparedStatement.execute("DELETE FROM Reviews;");
        } catch (SQLException e) {
            rollback();
            throw e;
        }
    }

    /**
     * Get the database connection.
     *
     * @return Connection
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new IllegalStateException("Connection is not established");
        }
        return connection;
    }
}
