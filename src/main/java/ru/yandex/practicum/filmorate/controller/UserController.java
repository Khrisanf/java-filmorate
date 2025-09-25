package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();


    @PostMapping
    public ResponseEntity<User> create(@RequestBody @Valid User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("User name is blank, fallback to login='{}'", user.getLogin());
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.debug("User login is blank, fallback to login='{}'", user.getLogin());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Login is blank");
        }
        int id = setUserId();
        user.setId(id);
        users.put(id, user);
        log.info("User created: id={}, login='{}'", id, user.getLogin());
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/{id}")
    public User update(@PathVariable int id, @RequestBody @Valid User user) {
        User existing = users.get(id);
        if (existing == null) {
            log.warn("Update failed: user {} not found", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User " + id + " not found");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("User name is blank on update, fallback to login='{}'", user.getLogin());
        }
        user.setId(id);
        users.put(id, user);
        log.info("User updated: id={}, login='{}'", id, user.getLogin());
        return user;
    }

    @GetMapping("/{id}")
    public User getOne(@PathVariable int id) {
        User user = users.get(id);
        if (user == null) {
            log.warn("Get one failed: user {} not found", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User " + id + " not found");
        }
        return user;
    }

    @GetMapping
    public Collection<User> getAll() {
        log.info("Get all users");
        return users.values();
    }

    private int setUserId() {
        int currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}

