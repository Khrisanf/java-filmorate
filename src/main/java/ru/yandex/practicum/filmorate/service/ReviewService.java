package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.event.EventOperation;
import ru.yandex.practicum.filmorate.model.event.EventType;
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

    @Transactional
    public Review createReview(Review review) {
        log.info("Создание отзыва пользователем {} для фильма {}", review.getUserId(), review.getFilmId());
        validateId(review.getUserId(), "userId");
        validateId(review.getFilmId(), "filmId");
        Review created = reviewStorage.save(review);
        feedService.addEvent(review.getUserId(),
                review.getUserId(),
                EventType.REVIEW,
                EventOperation.ADD,
                review.getReviewId(),
                "REVIEW");
        log.info("Отзыв создан с id {}", created.getReviewId());
        return created;
    }

    @Transactional
    public Review updateReview(Review review) {
        log.info("Обновление отзыва с id {}", review.getReviewId());
        Review existingReview = reviewStorage.findById(review.getReviewId())
                .orElseThrow(() -> new NotFoundException("Отзыва с id " + review.getReviewId() + " нет"));
        existingReview.setContent(review.getContent());
        existingReview.setIsPositive(review.getIsPositive());
        Review updated = reviewStorage.save(existingReview);

        feedService.addEvent(updated.getUserId(),
                updated.getUserId(),
                EventType.REVIEW,
                EventOperation.UPDATE,
                updated.getReviewId(),
                "REVIEW");
        log.info("Отзыв с id {} обновлен", updated.getReviewId());
        return updated;
    }

    public Review getReview(Long reviewId) {
        log.info("Получение отзыва с id {}", reviewId);
        return reviewStorage.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв не найден"));
    }

    public List<Review> getReviewsByFilmId(Long filmId, int count) {
        log.info("Получение {} отзывов для фильма с id {}", count, filmId);
        return reviewStorage.findByFilmId(filmId, count);
    }

    public List<Review> getAllReviews(int count) {
        log.info("Получение {} последних отзывов", count);
        return reviewStorage.findAll(count);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        log.info("Удаление отзыва с id {}", reviewId);
        Review review = reviewStorage.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id " + reviewId + " не найден"));
        reviewStorage.delete(reviewId);

        feedService.addEvent(review.getUserId(),
                review.getUserId(),
                EventType.REVIEW,
                EventOperation.REMOVE,
                reviewId,
                "REVIEW");
        log.info("Отзыв с id {} удален", reviewId);
    }

    @Transactional
    public void addLike(Long reviewId, Long userId) {
        log.info("Добавление лайка отзыву {} пользователем {}", reviewId, userId);
        validateReviewAndUser(reviewId, userId);
        reviewStorage.addLike(reviewId, userId);
        log.info("Лайк добавлен отзыву {} пользователем {}", reviewId, userId);
    }

    @Transactional
    public void addDisLike(Long reviewId, Long userId) {
        log.info("Добавление дизлайка отзыву {} пользователем {}", reviewId, userId);
        validateReviewAndUser(reviewId, userId);
        reviewStorage.addDisLike(reviewId, userId);
        log.info("Дизлайк добавлен отзыву {} пользователем {}", reviewId, userId);
    }

    @Transactional
    public void removeReaction(Long reviewId, Long userId) {
        log.info("Удаление реакции отзыву {} пользователем {}", reviewId, userId);
        validateReviewAndUser(reviewId, userId);
        reviewStorage.removeReaction(reviewId, userId);
        log.info("Реакция удалена отзыву {} пользователем {}", reviewId, userId);
    }

    private void validateReviewAndUser(Long reviewId, Long userId) {
        reviewStorage.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id " + reviewId + " не найден"));
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    private void validateId(Long id, String fieldName) {
        if (id == null) {
            throw new IllegalArgumentException(fieldName + " обязателен");
        }
        if (id <= 0) {
            throw new NotFoundException(fieldName + " должен быть положительным числом");
        }
    }
}