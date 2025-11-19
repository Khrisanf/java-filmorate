package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
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

    private static Set<Film> refillFilmsToCheck(Set<Film> filmsToCheck, Collection<Film> userLikes, Collection<Film> otherUserLikes) {
        filmsToCheck.clear();
        filmsToCheck.addAll(userLikes);
        filmsToCheck.addAll(otherUserLikes);
        return filmsToCheck;
    }

    private static double calculateDistanceToOtherUser(Set<Film> filmsToCheck, Collection<Film> userLikes, Collection<Film> otherUserLikes) {
        double distanceToOtherUser = 0;
        for (Film film : filmsToCheck) {
            int userLikedFilm = userLikes.contains(film) ? 1 : 0;
            int otherUserLikedFilm = otherUserLikes.contains(film) ? 1 : 0;
            distanceToOtherUser += Math.pow(userLikedFilm - otherUserLikedFilm, 2);
        }
        distanceToOtherUser = Math.sqrt(distanceToOtherUser);
        return distanceToOtherUser;
    }

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
        feedService.addEvent(userId, userId, EventType.LIKE, EventOperation.ADD, filmId, "FILM");
        return filmStorage.addLike(filmId, userId);
    }

    @Transactional
    public Film removeLike(long filmId, long userId) {
        ensureFilmExists(filmId);
        ensureUserExists(userId);
        feedService.addEvent(userId, userId, EventType.LIKE, EventOperation.REMOVE, filmId, "FILM");
        return filmStorage.removeLike(filmId, userId);
    }

    @Transactional(readOnly = true)
    public Collection<Film> getPopular(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be greater than 0");
        }
        return filmStorage.findPopular(count);
    }

    @Transactional(readOnly = true)
    public Collection<Film> getPopular(int count, Integer genreId, Integer year) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be greater than 0");
        }
        if (genreId == null && year == null) {
            return filmStorage.findPopular(count);
        }

        return filmStorage.findPopular(count, genreId, year);
    }

    @Transactional(readOnly = true)
    public Collection<Film> getFilmsByDirector(int directorId, String sortBy) {
        directorService.findById(directorId);

        if ("year".equals(sortBy)) {
            return filmStorage.findFilmsByDirectorSortedByYear(directorId);
        } else if ("likes".equals(sortBy)) {
            return filmStorage.findFilmsByDirectorSortedByLikes(directorId);
        } else {
            throw new IllegalArgumentException("Invalid sort parameter. Use 'year' or 'likes'");
        }
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

    @Transactional(readOnly = true)
    public List<Film> getRecommendations(long userId) {
        ensureUserExists(userId);
        Optional<User> userIdWithMostSameLikes = findUserWithMostSameLikes(userId);
        if (userIdWithMostSameLikes.isPresent()) {
            return findNotSeenFilms(userId, userIdWithMostSameLikes.get().getId());
        } else {
            return Collections.emptyList();
        }
    }

    private Optional<User> findUserWithMostSameLikes(long userId) {
        Collection<Film> userLikes = filmStorage.findLikesByUserId(userId);
        SortedMap<Double, User> neighborDistance = new TreeMap<>();
        Set<Film> filmsToCheck = new HashSet<>();
        for (User user : userStorage.findAll()) {
            if (user.getId() == userId) {
                continue;
            }
            Collection<Film> otherUserLikes = filmStorage.findLikesByUserId(user.getId());
            filmsToCheck = refillFilmsToCheck(filmsToCheck, userLikes, otherUserLikes);
            double distanceToOtherUser = calculateDistanceToOtherUser(filmsToCheck, userLikes, otherUserLikes);
            neighborDistance.put(distanceToOtherUser, user);
        }
        if (!neighborDistance.isEmpty()) {
            return Optional.of(neighborDistance.pollFirstEntry().getValue());
        } else {
            return Optional.empty();
        }
    }

    private List<Film> findNotSeenFilms(long userId, long otherUserId) {
        Collection<Film> userLikes = filmStorage.findLikesByUserId(userId);
        Collection<Film> otherUserLikes = filmStorage.findLikesByUserId(otherUserId);
        return otherUserLikes.stream().filter(f -> !userLikes.contains(f)).toList();
    }

}
