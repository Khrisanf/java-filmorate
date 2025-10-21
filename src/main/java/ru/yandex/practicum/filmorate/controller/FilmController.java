package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.net.URI;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
@lombok.RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    public ResponseEntity<Film> create(@RequestBody @Valid Film film) {
        Film createdFilm = filmService.create(film);
        log.info("Film created: id={}, name='{}'", createdFilm.getId(), createdFilm.getName());
        return ResponseEntity
                .created(URI.create("/films/" + createdFilm.getId()))
                .body(createdFilm);
    }

    @PutMapping
    public Film update(@RequestBody @Valid Film film) {
        Film updatedFilm = filmService.update(film);
        if (updatedFilm == null) {
            log.warn("Updated failed: film {} not found", film.getId());
        }
        log.info("Film updated: id={}", updatedFilm.getId());
        return updatedFilm;
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable Long id) {
        if (id == null) {
            log.warn("Get one failed: film not found: id={}", id);
        }
        return filmService.findById(id);
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        log.info("Get all films");
        return filmService.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Film> deleteById(@PathVariable long id) {
        filmService.deleteById(id);
        log.info("Deleted id: {}", id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable long id, @PathVariable long userId) {
        log.info("Add like to id: {}, userId: {}", id, userId);
        return filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film removeLike(@PathVariable long id, @PathVariable long userId) {
        log.info("Remove like from id: {}, userId: {}", id, userId);
        return filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("Get popular films");
        return filmService.getPopular(count);
    }

}
