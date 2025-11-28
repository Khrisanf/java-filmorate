package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.event.FeedEvent;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.service.FeedService;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.net.URI;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FilmService filmService;
    private final FeedService feedService;

    @PostMapping
    public ResponseEntity<User> create(@RequestBody @Valid User user) {
        User created = userService.create(user);
        return ResponseEntity.created(URI.create("/users/" + created.getId())).body(created);
    }

    @PutMapping
    public ResponseEntity<User> update(@RequestBody @Valid User user) {
        User updated = userService.update(user);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> findById(@PathVariable long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Collection<User>> getAll() {
        Collection<User> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}/feed")
    public ResponseEntity<List<FeedEvent>> getUserFeed(@PathVariable("id") Long userId) {
        List<FeedEvent> feed = feedService.getUserFeed(userId);
        return ResponseEntity.ok(feed);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<User> addFriend(@PathVariable long id, @PathVariable long friendId) {
        return ResponseEntity.ok(userService.addFriends(id, friendId));
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<User> removeFriend(@PathVariable long id, @PathVariable long friendId) {
        return ResponseEntity.ok(userService.removeFriends(id, friendId));
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<Collection<User>> getFriends(@PathVariable long id) {
        return ResponseEntity.ok(userService.findFriends(id));
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<Collection<User>> getCommonFriends(@PathVariable long id, @PathVariable long otherId) {
        return ResponseEntity.ok(userService.findMutualFriends(id, otherId));
    }

    /**
     * Поиск рекомендаций фильмов
     */
    @GetMapping("/{id}/recommendations")
    public ResponseEntity<Collection<Film>> getRecommendations(@PathVariable long id) {
        return ResponseEntity.ok(filmService.getRecommendations(id));
    }

}
