package ru.yandex.practicum.filmorate.memory;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
@Profile("memFilms")
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(0);
    private final DirectorStorage directorStorage;

    public InMemoryFilmStorage(DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    @Override
    public Film create(Film film) {
        film.setId(nextId.incrementAndGet());
        if (film.getLikes() == null) film.setLikes(new HashSet<>());
        if (film.getGenres() == null) film.setGenres(new LinkedHashSet<>());
        if (film.getDirectors() == null) film.setDirectors(new LinkedHashSet<>());
        films.put(film.getId(), film);
        directorStorage.updateFilmDirectors(film.getId(), film.getDirectors());
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        if (!films.containsKey(newFilm.getId())) {
            throw new NotFoundException("Film " + newFilm.getId() + " not found");
        }
        if (newFilm.getDirectors() == null) newFilm.setDirectors(new LinkedHashSet<>());
        films.put(newFilm.getId(), newFilm);
        directorStorage.updateFilmDirectors(newFilm.getId(), newFilm.getDirectors());
        return newFilm;
    }

    @Override
    public Optional<Film> findById(long id) {
        Film film = films.get(id);
        if (film != null) {
            film.setDirectors(directorStorage.findDirectorsByFilmId(id));
        }
        return Optional.ofNullable(film);
    }

    @Override
    public Collection<Film> findAll() {
        Collection<Film> allFilms = films.values();
        allFilms.forEach(film ->
                film.setDirectors(directorStorage.findDirectorsByFilmId(film.getId()))
        );
        return allFilms;
    }

    @Override
    public void deleteById(long id) {
        if (films.remove(id) == null) {
            throw new NotFoundException("Film " + id + " not found");
        }
        directorStorage.updateFilmDirectors(id, Set.of());
    }

    @Override
    public Film addLike(long filmId, long userId) {
        Film film = getOrThrow(filmId);
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        boolean added = film.getLikes().add(userId);
        if (added) {
            films.put(filmId, film);
        }
        return film;
    }

    @Override
    public Film removeLike(long filmId, long userId) {
        Film film = getOrThrow(filmId);
        boolean removed = film.getLikes().remove(userId);
        if (removed) {
            films.put(filmId, film);
        }
        return film;
    }

    @Override
    public Collection<Film> findPopular(int count) {
        return films.values().stream()
                .sorted(
                        Comparator
                                .comparingInt((Film f) -> f.getLikes() == null ? 0 : f.getLikes().size())
                                .reversed()
                                .thenComparingLong(Film::getId)
                )
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Film> findPopular(int count, Integer genreId, Integer year) {
        return films.values().stream()
                .filter(film -> {
                    if (genreId == null) {
                        return true;
                    }
                    if (film.getGenres() == null) {
                        return false;
                    }
                    return film.getGenres().stream()
                            .filter(Objects::nonNull)
                            .anyMatch(g -> Objects.equals(g.getId(), genreId));
                })
                .filter(film -> {
                    if (year == null) {
                        return true;
                    }
                    if (film.getReleaseDate() == null) {
                        return false;
                    }
                    return film.getReleaseDate().getYear() == year;
                })
                .sorted(
                        Comparator
                                .comparingInt((Film f) -> f.getLikes() == null ? 0 : f.getLikes().size())
                                .reversed()
                                .thenComparingLong(Film::getId)
                )
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Film> findLikesByUserId(long userId) {
        Collection<Film> likedFilms = new ArrayList<>();
        for (Film film : films.values()) {
            if (film.getLikes().contains(userId)) {
                likedFilms.add(film);
            }
        }
        return likedFilms;
    }


    @Override
    public Collection<Film> findFilmsByDirectorSortedByYear(int directorId) {
        return films.values().stream()
                .filter(film -> film.getDirectors().stream()
                        .anyMatch(d -> d.getId().equals(directorId)))
                .sorted(Comparator.comparing(Film::getReleaseDate))
                .peek(film -> film.setDirectors(directorStorage.findDirectorsByFilmId(film.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Film> findFilmsByDirectorSortedByLikes(int directorId) {
        return films.values().stream()
                .filter(film -> film.getDirectors().stream()
                        .anyMatch(d -> d.getId().equals(directorId)))
                .sorted(Comparator
                        .comparingInt((Film f) -> f.getLikes() == null ? 0 : f.getLikes().size())
                        .reversed()
                        .thenComparingLong(Film::getId))
                .peek(film -> film.setDirectors(directorStorage.findDirectorsByFilmId(film.getId())))
                .collect(Collectors.toList());
    }

    private Film getOrThrow(long id) {
        Film film = films.get(id);
        if (film == null) {
            throw new NotFoundException("Film " + id + " not found");
        }
        return film;
    }
}
