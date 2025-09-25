package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    Map<Long, Film> films = new HashMap<>();

    @PostMapping
    public ResponseEntity<Film> create(@RequestBody @Valid Film film) {
        long id = setFilmId();
        film.setId(id);
        films.put(id, film);
        log.info("Film created: id={}, name='{}'", id, film.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(film);
    }

    @PutMapping("/{id}")
    public Film update(@PathVariable long id, @RequestBody @Valid Film film) {
        Film existing = films.get(id);
        if (existing == null) {
            log.warn("Update failed: film {} not found", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Film " + id + " not found");
        }
        film.setId(id);
        films.put(id, film);
        log.info("Film updated: id={}, name='{}'", id, film.getName());
        return film;
    }

    @GetMapping("/{id}")
    public Film getOne(@PathVariable long id) {
        Film film = films.get(id);
        if (film == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Film " + id + " not found");
        }
        return film;
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    private long setFilmId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
