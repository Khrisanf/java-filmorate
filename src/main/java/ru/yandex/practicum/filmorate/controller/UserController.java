package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.net.URI;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
@lombok.RequiredArgsConstructor
public class UserController {
    private final UserService userService;


    @PostMapping
    public ResponseEntity<User> create(@RequestBody @Valid User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("User name is blank, fallback to login='{}'", user.getLogin());
        }
        User createdUser = userService.create(user);
        return ResponseEntity.created(URI.create("/users/" + createdUser.getId()))
                .body(createdUser);
    }

    @PutMapping
    public User update(@RequestBody @Valid User user) {
        User updatedUser = userService.update(user);
        if (updatedUser == null) {
            log.warn("Update failed: user {} not found", user.getId());
            throw new NotFoundException("User " + user.getId() + " not found");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("User name is blank on update, fallback to login='{}'", user.getLogin());
        }
        log.info("User updated: id={}, login='{}'", user.getId(), user.getLogin());
        return user;
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable long id) {
        log.debug("Find user by id: id={}", id);
        return userService.findById(id);
    }

    @GetMapping
    public Collection<User> getAll() {
        log.info("Get all users");
        return userService.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<User> deleteById(@PathVariable int id) {
        userService.deleteById(id);
        log.debug("Delete user by id: id={}", id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable long id, @PathVariable long friendId) {
        log.debug("Add friend to user by id: id={}, friendId={}", id, friendId);
        return userService.addFriends(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User removeFriend(@PathVariable long id, @PathVariable long friendId) {
        log.debug("Remove friend from user by id: id={}, friendId={}", id, friendId);
        return userService.removeFriends(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getFriends(@PathVariable long id) {
        log.debug("Get friends by id: id={}", id);
        return userService.findFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable long id, @PathVariable long otherId) {
        log.debug("Get friends by id: id={}, otherId={}", id, otherId);
        return userService.findMutualFriends(id, otherId);
    }
}
