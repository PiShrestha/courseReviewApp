package edu.virginia.sde.reviews;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ReviewService {
    private final ReviewDatabase reviewDatabase;

    public ReviewService(ReviewDatabase reviewDatabase) {
        this.reviewDatabase = reviewDatabase;
    }

    public boolean checkReviewExists(int userId, int courseId) {
        try {
            List<Review> userReviews = reviewDatabase.getReviewsByUserId(userId);

            return userReviews.stream()
                    .anyMatch(review -> review.getCourseId() == courseId);
        } catch (SQLException e) {
            return false;
        }
    }

    public Optional<String> createReview(Review review) {
        if (review.getRating() < 1 || review.getRating() > 5) {
            return Optional.of("Rating must be an integer between 1 and 5.");
        }

        try {
            if (checkReviewExists(review.getUserId(), review.getCourseId())) {
                return Optional.of("You have already reviewed this course.");
            }
            reviewDatabase.addReview(review);
            return Optional.empty();
        } catch (SQLException e) {
            return Optional.of("Error adding review. Please try again later.");
        }
    }

    public Optional<String> updateReview(Review review) {
        if (review.getRating() < 1 || review.getRating() > 5) {
            return Optional.of("Rating must be an integer between 1 and 5.");
        }

        try {
            Optional<Review> existingReview = reviewDatabase.getReviewById(review.getId());
            if (existingReview.isEmpty()) {
                return Optional.of("No existing review found to update.");
            }
            reviewDatabase.updateReview(review);
            return Optional.empty();
        } catch (SQLException e) {
            return Optional.of("Error updating review. Please try again later.");
        }
    }

    public Optional<String> deleteReview(int reviewId) {
        try {
            Optional<Review> existingReview = reviewDatabase.getReviewById(reviewId);
            if (existingReview.isEmpty()) {
                return Optional.of("No existing review found to delete.");
            }
            reviewDatabase.deleteReview(reviewId);
            return Optional.empty();
        } catch (SQLException e) {
            return Optional.of("Error deleting review. Please try again later.");
        }
    }

    public double getAverageRatingForCourse(int courseId) {
        try {
            List<Review> reviews = reviewDatabase.getReviewsByCourseId(courseId);

            if (reviews.isEmpty()) {
                return 0.0;
            }

            // Calculate the sum of ratings
            int totalRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .sum();

            // Calculate the average
            return (double) totalRating / reviews.size();
        } catch (SQLException e) {
            throw new RuntimeException("Error calculating average rating for course.", e);
        }
    }

    public List<Review> getReviewsForCourse(int courseId) {
        try {
            return reviewDatabase.getReviewsByCourseId(courseId);
        } catch (SQLException e) {
            return List.of();
        }
    }

    public List<Review> getReviewsByUser(int userId) {
        try {
            return reviewDatabase.getReviewsByUserId(userId);
        } catch (SQLException e) {
            return List.of();
        }
    }
}
