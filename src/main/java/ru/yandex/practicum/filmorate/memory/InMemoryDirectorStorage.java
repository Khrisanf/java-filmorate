package ru.yandex.practicum.filmorate.memory;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@Profile("memFilms")
public class InMemoryDirectorStorage implements DirectorStorage {
    private final Map<Integer, Director> directors = new ConcurrentHashMap<>();
    private final Map<Long, Set<Director>> filmDirectors = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    @Override
    public Collection<Director> findAll() {
        return directors.values().stream()
                .sorted(Comparator.comparingInt(Director::getId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Director> findById(int id) {
        return Optional.ofNullable(directors.get(id));
    }

    @Override
    public Director create(Director director) {
        director.setId(nextId.getAndIncrement());
        directors.put(director.getId(), director);
        return director;
    }

    @Override
    public Director update(Director director) {
        if (!directors.containsKey(director.getId())) {
            throw new NotFoundException("Director with id " + director.getId() + " not found");
        }
        directors.put(director.getId(), director);
        return director;
    }

    @Override
    public void deleteById(int id) {
        // Сначала удаляем связи с фильмами
        filmDirectors.entrySet().forEach(entry ->
                entry.getValue().removeIf(d -> d.getId().equals(id))
        );

        // Затем удаляем самого режиссера
        if (directors.remove(id) == null) {
            throw new NotFoundException("Director with id " + id + " not found");
        }
    }

    @Override
    public Set<Integer> findMissingIds(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) return Set.of();
        Set<Integer> missing = new HashSet<>(ids);
        missing.removeAll(directors.keySet());
        return missing;
    }

    @Override
    public void updateFilmDirectors(long filmId, Set<Director> directors) {
        if (directors == null || directors.isEmpty()) {
            filmDirectors.remove(filmId);
        } else {
            Set<Director> normalized = directors.stream()
                    .filter(Objects::nonNull)
                    .filter(d -> d.getId() != null)
                    .collect(Collectors.toCollection(
                            () -> new TreeSet<>(Comparator.comparingInt(Director::getId))
                    ));
            filmDirectors.put(filmId, normalized);
        }
    }

    @Override
    public Set<Director> findDirectorsByFilmId(long filmId) {
        return filmDirectors.getOrDefault(filmId, new LinkedHashSet<>());
    }

    @Override
    public Collection<Director> findDirectorsByFilmIds(Collection<Long> filmIds) {
        return filmDirectors.entrySet().stream()
                .filter(entry -> filmIds.contains(entry.getKey()))
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toSet());
    }
}
