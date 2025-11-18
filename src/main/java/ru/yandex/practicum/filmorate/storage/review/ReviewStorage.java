package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Optional<Review> findById(Long reviewId);

    Review save(Review review);

    void delete(Long reviewId);

    List<Review> findByFilmId(Long filmId, int count);

    List<Review> findAll(int count);

    void addLike(Long reviewId, Long userId);

    void addDisLike(Long reviewId, Long userId);

    void removeReaction(Long reviewId, Long userId);
}
