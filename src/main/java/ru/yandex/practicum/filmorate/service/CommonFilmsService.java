package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class CommonFilmsService {

    private final FilmDbStorage filmDbStorage;

    public Collection<Film> getCommonFilms(Long userId, Long friendsId) {
        return filmDbStorage.findCommonFilms(userId, friendsId);
    }
}
