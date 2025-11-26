package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidatorException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.event.EventOperation;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FeedService feedService;
    private final FilmStorage filmStorage;

    @Transactional
    public Review createReview(Review review) {
        getUserById(review.getUserId());
        getFilmById(review.getFilmId());

        if (reviewStorage.existsByUserIdAndFilmId(review.getUserId(), review.getFilmId())) {
            throw new ValidatorException("Review already exists");
        }

        Review created = reviewStorage.save(review);

        feedService.addEvent(created.getUserId(),
                created.getUserId(),
                EventType.REVIEW,
                EventOperation.ADD,
                created.getReviewId(),
                "REVIEW");

        log.info("Review with id {} created.", created.getReviewId());

        return created;
    }

    @Transactional
    public Review updateReview(Review review) {
        log.info("Updating review with id {}", review.getReviewId());
        Review existingReview = getReview(review.getReviewId());
        existingReview.setContent(review.getContent());
        existingReview.setIsPositive(review.getIsPositive());
        Review updated = reviewStorage.update(existingReview);

        feedService.addEvent(updated.getUserId(),
                updated.getUserId(),
                EventType.REVIEW,
                EventOperation.UPDATE,
                updated.getReviewId(),
                "REVIEW");
        log.info("Review with id {} updated", updated.getReviewId());
        return updated;
    }


    @Transactional
    public void deleteReview(Long reviewId) {
        log.info("Deleting review with id {}", reviewId);
        Review review = getReview(reviewId);
        feedService.addEvent(review.getUserId(),
                review.getUserId(),
                EventType.REVIEW,
                EventOperation.REMOVE,
                reviewId,
                "REVIEW");
        reviewStorage.delete(reviewId);
        log.info("Review with id {} deleted", reviewId);
    }

    @Transactional
    public void addLike(Long reviewId, Long userId) {
        log.info("Adding like to review {} by user {}", reviewId, userId);
        getReview(reviewId);
        getUserById(userId);
        reviewStorage.addLike(reviewId, userId);
        log.info("Like added to review {} by user {}", reviewId, userId);
    }

    @Transactional
    public void addDisLike(Long reviewId, Long userId) {
        log.info("Adding dislike to review {} by user {}", reviewId, userId);
        getReview(reviewId);
        getUserById(userId);
        reviewStorage.addDisLike(reviewId, userId);
        log.info("Dislike added to review {} by user {}", reviewId, userId);
    }

    @Transactional
    public void removeReaction(Long reviewId, Long userId) {
        log.info("Removing reaction from review {} by user {}", reviewId, userId);
        getReview(reviewId);
        getUserById(userId);
        reviewStorage.removeReaction(reviewId, userId);
        log.info("Reaction removed from review {} by user {}", reviewId, userId);
    }

    private void getUserById(long userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
    }

    private void getFilmById(long filmId) {
        filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Film with id " + filmId + " not found"));
    }

    public Review getReview(Long reviewId) {
        log.info("Getting review with id {}", reviewId);
        return reviewStorage.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found"));
    }

    public List<Review> getReviewsByFilmId(Long filmId, int count) {
        log.info("Getting {} reviews for film with id {}", count, filmId);
        return reviewStorage.findByFilmId(filmId, count);
    }

    public List<Review> getAllReviews(int count) {
        log.info("Getting {} latest reviews", count);
        return reviewStorage.findAll(count);
    }
}