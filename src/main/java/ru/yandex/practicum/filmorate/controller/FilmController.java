package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.SearchBy;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    public ResponseEntity<Film> create(@RequestBody @Valid Film film) {
        Film created = filmService.create(film);
        URI location = URI.create("/films/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping
    public ResponseEntity<Film> update(@RequestBody Film film) {
        Film updatedFilm = filmService.update(film);
        return ResponseEntity.ok(updatedFilm);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> findById(@PathVariable long id) {
        return ResponseEntity.ok(filmService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Collection<Film>> getAllFilms() {
        Collection<Film> films = filmService.findAll();
        return ResponseEntity.ok(films);
    }

    @GetMapping("/common")
    public ResponseEntity<Collection<Film>> getCommonFilms(
            @RequestParam Long userId,
            @RequestParam Long friendId) {

        Collection<Film> commonFilms = filmService.getCommonFilms(userId, friendId);
        return ResponseEntity.ok(commonFilms);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Film> deleteById(@PathVariable long id) {
        filmService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Film> addLike(@PathVariable long id, @PathVariable long userId) {
        return ResponseEntity.ok(filmService.addLike(id, userId));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Film> removeLike(@PathVariable long id, @PathVariable long userId) {
        return ResponseEntity.ok(filmService.removeLike(id, userId));
    }

    @GetMapping("/popular")
    public ResponseEntity<Collection<Film>> getPopularFilms(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year
    ) {
        Collection<Film> films = filmService.getPopular(count, genreId, year);
        return ResponseEntity.ok(films);
    }

    @GetMapping("/director/{directorId}")
    public ResponseEntity<Collection<Film>> getFilmsByDirector(
            @PathVariable int directorId,
            @RequestParam String sortBy) {
        Collection<Film> films = filmService.getFilmsByDirector(directorId, sortBy);
        return ResponseEntity.ok(films);
    }

    @GetMapping("/search")
    public List<Film> searchFilms(
            @RequestParam String query,
            @RequestParam String by
    ) {
        Set<SearchBy> searchBy = parseSearchBy(by);
        return filmService.search(query, searchBy);
    }

    private Set<SearchBy> parseSearchBy(String by) {
        return Arrays.stream(by.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .map(SearchBy::valueOf)
                .collect(Collectors.toSet());
    }
}


