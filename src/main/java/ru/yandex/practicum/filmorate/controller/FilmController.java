package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @PostMapping
    public ResponseEntity<Film> create(@RequestBody @Valid Film film) {
        film.setId(getNextId());
        films.put(getNextId(), film);
        log.info("Film created: id={}, name='{}'", getNextId(), film.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(film);
    }

    @PutMapping
    public Film update(@RequestBody @Valid Film film) {
        Film existing = films.get(film.getId());
        if (existing == null) {
            log.warn("Update failed: film {} not found", film.getId());
            throw new NotFoundException("Film " + film.getId() + " not found");
        }
        film.setId(film.getId());
        films.put(film.getId(), film);
        log.info("Film updated: id={}, name='{}'", film.getId(), film.getName());
        return film;
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable long id) {
        Film film = films.get(id);
        if (film == null) {
            throw new NotFoundException("Film " + film.getId() + " not found");
        }
        return film;
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
