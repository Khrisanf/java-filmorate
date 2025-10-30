package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film create(Film film) {
        Film created = filmStorage.create(film);
        log.info("Film created: id={}, name='{}'", created.getId(), created.getName());
        return created;
    }

    public Film update(Film film) {
        ensureFilmExists(film.getId());
        Film updated = filmStorage.update(film);
        log.info("Film updated: id={}", updated.getId());
        return updated;
    }

    public Film findById(long id) {
        Film film = ensureFilmExists(id);
        log.debug("Film fetched: id={}", id);
        return film;
    }

    public Collection<Film> findAll() {
        Collection<Film> all = filmStorage.findAll();
        log.info("Get all films: count={}", all.size());
        return all;
    }

    public void deleteById(long id) {
        filmStorage.deleteById(id);
        log.info("Film deleted: id={}", id);
    }

    public Film addLike(long filmId, long userId) {
        ensureUserExists(userId);
        return filmStorage.addLike(filmId, userId);
    }

    public Film removeLike(long filmId, long userId) {
        ensureUserExists(userId);
        return filmStorage.removeLike(filmId, userId);
    }

    public Collection<Film> getPopular(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be greater than 0");
        }
        return filmStorage.findPopular(count);
    }

    private Film ensureFilmExists(long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Film not found"));
    }

    private void ensureUserExists(long id) {
        userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
