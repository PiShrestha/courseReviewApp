package edu.virginia.sde.reviews;
import java.sql.Timestamp;

public class Review {
    private int id;
    private int courseId;
    private int userId;
    private int rating;
    private String comment;
    private Timestamp timestamp;

    public Review(int id, int courseId, int userId, int rating, String comment, Timestamp timestamp) {
        this.id = id;
        this.courseId = courseId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public Review(int courseId, int userId, int rating, String comment, Timestamp timestamp) {
        this.courseId = courseId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public Review() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Review ID: " + id + ", Course ID: " + courseId +
                ", User ID: " + userId + ", Rating: " + rating +
                ", Timestamp: " + timestamp +
                ", Comment: " + (comment == null || comment.isEmpty() ? "None" : comment);
                // If there is a comment, then it will return the comment else, None.
    }
}
