package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.event.FeedEventResponse;
import ru.yandex.practicum.filmorate.service.FeedService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping("/users/{id}/feed")
    public ResponseEntity<List<FeedEventResponse>> getUserFeed(@PathVariable("id") Long userId) {
        List<FeedEventResponse> feed = feedService.getUserFeed(userId);
        return ResponseEntity.ok(feed);
    }
}