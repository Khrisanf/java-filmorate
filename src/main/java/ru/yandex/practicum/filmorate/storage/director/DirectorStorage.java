package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.film.Director;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface DirectorStorage {
    Collection<Director> findAll();

    Optional<Director> findById(int id);

    Director create(Director director);

    Director update(Director director);

    void deleteById(int id);

    Set<Integer> findMissingIds(Set<Integer> ids);

    void updateFilmDirectors(long filmId, Set<Director> directors);

    Set<Director> findDirectorsByFilmId(long filmId);

    Collection<Director> findDirectorsByFilmIds(Collection<Long> filmIds);
}