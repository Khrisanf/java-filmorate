package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.SearchBy;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.event.EventOperation;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreService genreService;
    private final MpaService mpaService;
    private final FeedService feedService;
    private final DirectorService directorService;

   @Transactional
    public Film create(Film film) {
        validateMpaAndGenre(film);
        validateDirectors(film);
        Film created = filmStorage.create(fillGenres(fillDirectors(film)));
        log.info("Film created: id={}, name='{}'", created.getId(), created.getName());
        return created;
    }

    @Transactional
    public Film update(Film film) {
        ensureFilmExists(film.getId());
        validateMpaAndGenre(film);
        validateDirectors(film);
        Film updated = filmStorage.update(fillGenres(fillDirectors(film)));
        log.info("Film updated: id={}", updated.getId());
        return updated;
    }

    @Transactional(readOnly = true)
    public Film findById(long id) {
        Film film = ensureFilmExists(id);
        log.debug("Film fetched: id={}", id);
        return film;
    }

    @Transactional(readOnly = true)
    public Collection<Film> findAll() {
        Collection<Film> all = filmStorage.findAll();
        log.info("Get all films: count={}", all.size());
        return all;
    }

    @Transactional
    public void deleteById(long id) {
        filmStorage.deleteById(id);
        log.info("Film deleted: id={}", id);
    }

    @Transactional
    public Film addLike(long filmId, long userId) {
        ensureFilmExists(filmId);
        ensureUserExists(userId);
        feedService.addEvent(userId, EventType.LIKE, EventOperation.ADD, filmId);
        return filmStorage.addLike(filmId, userId);
    }

    @Transactional
    public Film removeLike(long filmId, long userId) {
        ensureFilmExists(filmId);
        ensureUserExists(userId);
        feedService.addEvent(userId, EventType.LIKE, EventOperation.REMOVE, filmId);
        return filmStorage.removeLike(filmId, userId);
    }

    @Transactional(readOnly = true)
    public Collection<Film> getPopular(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be greater than 0");
        }
        Collection<Film> popular = filmStorage.findPopular(count);
        return popular;
    }

    @Transactional(readOnly = true)
    public Collection<Film> getPopular(int count, Integer genreId, Integer year) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be greater than 0");
        }
        if (genreId == null && year == null) {
            return getPopular(count);
        }
        Collection<Film> popular = filmStorage.findPopular(count, genreId, year);
        return popular;
    }

    @Transactional(readOnly = true)
    public Collection<Film> getFilmsByDirector(int directorId, String sortBy) {
        // Проверяем что режиссер существует
        directorService.findById(directorId);

        Collection<Film> films;
        if ("year".equals(sortBy)) {
            films = filmStorage.findFilmsByDirectorSortedByYear(directorId);
        } else if ("likes".equals(sortBy)) {
            films = filmStorage.findFilmsByDirectorSortedByLikes(directorId);
        } else {
            throw new IllegalArgumentException("Invalid sort parameter. Use 'year' or 'likes'");
        }

        return films;
    }

    @Transactional(readOnly = true)
    public List<Film> getRecommendations(long userId) {
        ensureUserExists(userId);
        Optional<User> userIdWithMostSameLikes = findUserWithMostSameLikes(userId);
        if (userIdWithMostSameLikes.isPresent()) {
            List<Film> recommendations = findNotSeenFilms(userId, userIdWithMostSameLikes.get().getId());
            return recommendations;
        } else {
            return Collections.emptyList();
        }
    }

    public Collection<Film> getCommonFilms(Long userId, Long friendsId) {
        return filmStorage.findCommonFilms(userId, friendsId);
    }

    private Film ensureFilmExists(long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Film not found: id=" + id));
    }

    private void ensureUserExists(long id) {
        userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + id));
    }

    private void validateMpaAndGenre(Film f) {
        if (f.getMpa() != null && f.getMpa().getId() != null) {
            if (!mpaService.existsById(f.getMpa().getId()))
                throw new NotFoundException("MPA not found: id=" + f.getMpa().getId());
        }
        var genreIds = f.getGenres() == null ? Set.<Integer>of()
                : f.getGenres().stream()
                .filter(Objects::nonNull)
                .map(Genre::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        var missing = genreService.findMissingIds(genreIds);
        if (!missing.isEmpty())
            throw new NotFoundException("Genres not found: " + missing);
    }

    private void validateDirectors(Film film) {
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) {
            return;
        }

        var directorIds = film.getDirectors().stream()
                .filter(Objects::nonNull)
                .map(Director::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        var missing = directorService.findMissingIds(directorIds);
        if (!missing.isEmpty()) {
            throw new NotFoundException("Directors not found: " + missing);
        }
    }

    private Film fillGenres(Film f) {
        if (f.getGenres() == null || f.getGenres().isEmpty()) return f;
        var filled = f.getGenres().stream()
                .filter(Objects::nonNull)
                .filter(g -> g.getId() != null)
                .collect(Collectors.toCollection(
                        () -> new TreeSet<>(Comparator.comparingInt(Genre::getId))
                ));
        f.setGenres(filled);
        return f;
    }

    private Film fillDirectors(Film film) {
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) {
            return film;
        }

        var filled = film.getDirectors().stream()
                .filter(Objects::nonNull)
                .filter(d -> d.getId() != null)
                .collect(Collectors.toCollection(
                        () -> new TreeSet<>(Comparator.comparingInt(Director::getId))
                ));
        film.setDirectors(filled);
        return film;
    }

    private Optional<User> findUserWithMostSameLikes(long userId) {
        Collection<Long> filmIdsWithUserLikes = filmStorage.findFilmIdsLikedByUser(userId);
        Map<Long, Long> commonLikesCount = filmStorage.findCommonLikesCountByFilmIds(userId, filmIdsWithUserLikes);
        Optional<Optional<User>> foundUser = commonLikesCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .map(userStorage::findById);
        return foundUser.orElse(Optional.empty());
    }

    private List<Film> findNotSeenFilms(long userId, long otherUserId) {
        return filmStorage.findFilmsThatUserHasNotWatchedAndTheOtherWatched(userId, otherUserId);
    }

    @Transactional(readOnly = true)
    public List<Film> search(String query, Set<SearchBy> searchBy) {
        if (query == null || query.isBlank() || searchBy == null || searchBy.isEmpty()) {
            return List.of();
        }
        List<Film> films = filmStorage.search(query, searchBy);
        films.forEach(f -> log.info(
                "Search result film id={}, name={}, likes={}, date={}",
                f.getId(), f.getName(), f.getLikes().size(), f.getReleaseDate()
        ));

        return films;
    }

}
