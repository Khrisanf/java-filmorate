package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.SearchBy;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.search.SubstringMatcher;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmSearchService {

    private final FilmService filmService;
    private final SubstringMatcher matcher;

    public FilmSearchService(FilmService filmService, SubstringMatcher matcher) {
        this.filmService = filmService;
        this.matcher = matcher;
    }

    public List<Film> search(String query, Set<SearchBy> searchBy) {
        String normalizedQuery = normalize(query);
        if (normalizedQuery.isEmpty()) {
            return List.of();
        }

        Collection<Film> allFilms = filmService.findAll();

        Set<Film> result = new LinkedHashSet<>();

        for (Film film : allFilms) {
            boolean matched = false;

            if (searchBy.contains(SearchBy.TITLE)) {
                String title = normalize(film.getName());
                if (matcher.contains(title, normalizedQuery)) {
                    matched = true;
                }
            }

            if (matched) {
                result.add(film);
            }
        }

        return result.stream()
                .sorted(Comparator.comparingInt(this::getPopularity).reversed())
                .collect(Collectors.toList());
    }

    private String normalize(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    private int getPopularity(Film film) {
        return film.getLikes().size();
    }
}



