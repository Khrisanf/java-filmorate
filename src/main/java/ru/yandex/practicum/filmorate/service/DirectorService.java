package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public Collection<Director> findAll() {
        return directorStorage.findAll();
    }

    public Director findById(int id) {
        return getDirectorOrThrow(id);
    }

    public Director create(Director director) {
        return directorStorage.create(director);
    }

    public Director update(Director director) {
        getDirectorOrThrow(director.getId());
        return directorStorage.update(director);
    }

    public void deleteById(int id) {
        getDirectorOrThrow(id);
        directorStorage.deleteById(id);
    }

    public Set<Integer> findMissingIds(Set<Integer> ids) {
        return directorStorage.findMissingIds(ids);
    }

    private Director getDirectorOrThrow(int id) {
        return directorStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Director with id " + id + " not found"));
    }
}