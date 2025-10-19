package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;

@Service
@lombok.RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;

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

}
