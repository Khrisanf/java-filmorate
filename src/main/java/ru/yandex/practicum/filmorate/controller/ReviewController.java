package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<Review> create(@RequestBody Review review) {
        Review created = reviewService.createReview(review);
        URI location = URI.create("/reviews/" + created.getReviewId());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping
    public ResponseEntity<Review> update(@RequestBody Review review) {
        Review updated = reviewService.updateReview(review);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> findById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReview(id));
    }

    @GetMapping
    public ResponseEntity<List<Review>> findAll(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10") int count) {
        List<Review> reviews = filmId != null
                ? reviewService.getReviewsByFilmId(filmId, count)
                : reviewService.getAllReviews(count);
        return ResponseEntity.ok(reviews);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Review> addLike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.addLike(id, userId);
        return ResponseEntity.ok(reviewService.getReview(id));
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Review> addDislike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.addDisLike(id, userId);
        return ResponseEntity.ok(reviewService.getReview(id));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Review> removeLike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.removeReaction(id, userId);
        return ResponseEntity.ok(reviewService.getReview(id));
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Review> removeDislike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.removeReaction(id, userId);
        return ResponseEntity.ok(reviewService.getReview(id));
    }
}