package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.SearchBy;
import ru.yandex.practicum.filmorate.model.film.Film;

import java.util.*;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    Optional<Film> findById(long id);

    Collection<Film> findAll();

    void deleteById(long id);

    Film addLike(long filmId, long userId);

    Film removeLike(long filmId, long userId);

    Collection<Film> findPopular(int count);

    Collection<Long> findFilmIdsLikedByUser(long userId);

    Collection<Film> findPopular(int count, Integer genreId, Integer year);

    Collection<Film> findFilmsByDirectorSortedByYear(int directorId);

    Collection<Film> findFilmsByDirectorSortedByLikes(int directorId);

    List<Film> search(String query, Set<SearchBy> searchBy);

    Collection<Film> findCommonFilms(long userId, long friendId);

    Map<Long, Long> findCommonLikesCountByFilmIds(Long userIdToExclude, Collection<Long> filmIds);

    List<Film> findFilmsThatUserHasNotWatchedAndTheOtherWatched(long userId, long otherUserId);

}
