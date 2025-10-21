package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@lombok.RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        filmStorage.findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Film not found"));
        return filmStorage.update(film);
    }

    public Film findById(long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Film not found"));
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public void deleteById(long id) {
        filmStorage.deleteById(id);
    }

    public Film addLike(long filmId, long userId) {
        Film film = findById(filmId);
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        film.getLikes().add(userId);
        return filmStorage.update(film);
    }

    public Film removeLike(long filmId, long userId) {
        Film film = findById(filmId);
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        film.getLikes().remove(userId);
        return filmStorage.update(film);
    }

    public Collection<Film> getPopular(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be greater than 0");
        }
        return filmStorage.findAll().stream()
                .sorted((a, b) -> Integer.compare(b.getLikes().size(), a.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

}
