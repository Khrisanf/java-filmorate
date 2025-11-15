package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review save(Review review) {
        if (review.getReviewId() == null || review.getReviewId() == 0) {
            String sql = "INSERT INTO review (user_id, film_id, content, is_positive, useful) VALUES (?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"review_id"});
                ps.setLong(1, review.getUserId());
                ps.setLong(2, review.getFilmId());
                ps.setString(3, review.getContent());
                ps.setBoolean(4, review.getIsPositive());
                ps.setInt(5, review.getUseful());
                return ps;
            }, keyHolder);
            review.setReviewId(keyHolder.getKey().longValue());
            return review;
        } else {
            String sql = "UPDATE review SET content = ?, is_positive = ?, useful = ? WHERE review_id = ?";
            jdbcTemplate.update(sql, review.getContent(), review.getIsPositive(), review.getUseful(), review.getReviewId());
            return review;
        }
    }

    @Override
    public Optional<Review> findById(Long reviewId) {
        String sql = "SELECT * FROM review WHERE review_id = ?";
        List<Review> review = jdbcTemplate.query(sql, this::mapRowToReview, reviewId);
        return review.isEmpty() ? Optional.empty() : Optional.of(review.get(0));
    }

    @Override
    public List<Review> findByFilmId(Long filmId, int count) {
        String sql = "SELECT * FROM review WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
        return jdbcTemplate.query(sql, this::mapRowToReview, filmId, count);
    }

    @Override
    public List<Review> findAll(int count) {
        String sql = "SELECT * FROM review ORDER BY useful DESC LIMIT ?";
        return jdbcTemplate.query(sql, this::mapRowToReview, count);
    }

    @Override
    public void delete(Long reviewId) {
        String sql = "DELETE FROM review WHERE review_id = ?";
        int rowsDeleted = jdbcTemplate.update(sql, reviewId);
        if (rowsDeleted == 0) {
            throw new NotFoundException("Review not found with id: " + reviewId);
        }
    }

    @Override
    public void addLike(Long reviewId, Long userId) {
        try {
            String insertSql = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, true)";
            jdbcTemplate.update(insertSql, reviewId, userId);
            updateUseful(reviewId, 1);
        } catch (Exception e) {
            String updateSql = "UPDATE review_likes SET is_like = true WHERE review_id = ? AND user_id = ? AND is_like = false";
            int updated = jdbcTemplate.update(updateSql, reviewId, userId);
            if (updated > 0) {
                updateUseful(reviewId, 2);
            }
        }
    }

    @Override
    public void addDisLike(Long reviewId, Long userId) {
        try {
            String insertSql = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, false)";
            jdbcTemplate.update(insertSql, reviewId, userId);
            updateUseful(reviewId, -1);
        } catch (Exception e) {
            String updateSql = "UPDATE review_likes SET is_like = false WHERE review_id = ? AND user_id = ? AND is_like = true";
            int updated = jdbcTemplate.update(updateSql, reviewId, userId);
            if (updated > 0) {
                updateUseful(reviewId, -2);
            }
        }
    }

    @Override
    public void removeReaction(Long reviewId, Long userId) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, reviewId, userId);
        recalculateUseful(reviewId);
    }

    private void updateUseful(Long reviewId, int delta) {
        String sql = "UPDATE review SET useful = useful + ? WHERE review_id = ?";
        jdbcTemplate.update(sql, delta, reviewId);
    }

    private void recalculateUseful(Long reviewId) {
        String sql = "SELECT (SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND is_like = true) - " +
                "(SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND is_like = false) AS useful";
        int useful = jdbcTemplate.queryForObject(sql, Integer.class, reviewId, reviewId);
        jdbcTemplate.update("UPDATE review SET useful = ? WHERE review_id = ?", useful, reviewId);
    }

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getLong("review_id"));
        review.setContent(rs.getString("content"));
        review.setUseful(rs.getInt("useful"));
        review.setUserId(rs.getLong("user_id"));
        review.setFilmId(rs.getLong("film_id"));
        review.setIsPositive(rs.getBoolean("is_positive"));
        return review;
    }
}
